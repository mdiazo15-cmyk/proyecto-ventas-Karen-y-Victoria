package edu.umg.ventas.validacion;

import edu.umg.ventas.modelo.RegistroCrudo;
import edu.umg.ventas.modelo.RegistroRechazado;
import edu.umg.ventas.modelo.Venta;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class ValidadorVenta {

    // STRICT: rechaza fechas que no existen, como 2026-02-31
    private static final List<DateTimeFormatter> FORMATOS_FECHA = List.of(
        DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));

    private static final Pattern PATRON_CODIGO = Pattern.compile("^[A-Z0-9][A-Z0-9_-]{1,29}$");
    private static final int CANTIDAD_MAXIMA = 10_000;
    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("1000000");

    // Memoria de ids ya aceptados (entre todos los archivos): id -> "archivo #registro"
    private final Map<String, String> idsAceptados = new HashMap<>();

    /** Procesa una lista de registros crudos. Un registro malo no detiene a los demás. */
    public ResultadoValidacion procesar(List<RegistroCrudo> crudos) {
        List<Venta> validas = new ArrayList<>();
        List<RegistroRechazado> rechazados = new ArrayList<>();

        for (RegistroCrudo r : crudos) {
            String idCrudo = limpiar(r.getCampo("id_venta")).toUpperCase(Locale.ROOT);
            String idParaReporte = idCrudo.isEmpty() ? null : idCrudo;
            try {
                Venta venta = convertir(r);
                String origen = r.getArchivo() + " #" + r.getNumero();
                String previo = idsAceptados.putIfAbsent(venta.getIdVenta(), origen);
                if (previo != null) {
                    rechazados.add(new RegistroRechazado(r.getArchivo(), r.getNumero(), idParaReporte,
                        "id_venta duplicado: ya fue aceptado en " + previo, true));
                } else {
                    validas.add(venta);
                }
            } catch (ValidacionException e) {
                rechazados.add(new RegistroRechazado(r.getArchivo(), r.getNumero(), idParaReporte,
                    e.getMessage(), false));
            }
        }
        return new ResultadoValidacion(validas, rechazados);
    }

    /** Convierte un registro crudo en Venta, normalizando. Lanza excepción si es inválido. */
    public Venta convertir(RegistroCrudo r) throws ValidacionException {
        String id = limpiar(r.getCampo("id_venta")).toUpperCase(Locale.ROOT);
        if (id.isEmpty()) {
            throw new ValidacionException("id_venta vacío");
        }
        if (id.length() > 50) {
            throw new ValidacionException("id_venta demasiado largo (máx. 50 caracteres)");
        }

        LocalDate fecha = validarFecha(r.getCampo("fecha"));

        String sucursal = limpiar(r.getCampo("sucursal"));
        if (sucursal.isEmpty()) {
            throw new ValidacionException("sucursal vacía");
        }
        sucursal = tituloNombre(sucursal);

        String codigo = limpiar(r.getCampo("codigo_producto")).toUpperCase(Locale.ROOT);
        if (codigo.isEmpty()) {
            throw new ValidacionException("codigo_producto vacío");
        }
        if (!PATRON_CODIGO.matcher(codigo).matches()) {
            throw new ValidacionException("codigo_producto con formato inválido: '" + codigo + "'");
        }

        String producto = limpiar(r.getCampo("producto"));
        if (producto.isEmpty()) {
            throw new ValidacionException("producto vacío");
        }
        producto = tituloProducto(producto);

        int cantidad = validarCantidad(r.getCampo("cantidad"));
        BigDecimal precio = validarPrecio(r.getCampo("precio_unitario"));

        String vendedor = limpiar(r.getCampo("vendedor"));
        if (vendedor.isEmpty()) {
            throw new ValidacionException("vendedor vacío");
        }
        vendedor = tituloNombre(vendedor);

        return new Venta(id, fecha, sucursal, codigo, producto, cantidad, precio, vendedor, r.getArchivo());
    }

    private LocalDate validarFecha(String texto) throws ValidacionException {
        String t = limpiar(texto);
        if (t.isEmpty()) {
            throw new ValidacionException("fecha vacía");
        }
        for (DateTimeFormatter formato : FORMATOS_FECHA) {
            try {
                LocalDate fecha = LocalDate.parse(t, formato);
                if (fecha.isAfter(LocalDate.now())) {
                    throw new ValidacionException("fecha en el futuro: " + t);
                }
                if (fecha.getYear() < 2000) {
                    throw new ValidacionException("fecha fuera de rango (antes del 2000): " + t);
                }
                return fecha;
            } catch (DateTimeParseException e) {
                // se prueba el siguiente formato
            }
        }
        throw new ValidacionException("fecha inválida o con formato no reconocido: '" + t + "'");
    }

    private int validarCantidad(String texto) throws ValidacionException {
        String t = limpiar(texto);
        if (t.isEmpty()) {
            throw new ValidacionException("cantidad vacía");
        }
        int cantidad;
        try {
            cantidad = Integer.parseInt(t);
        } catch (NumberFormatException e) {
            throw new ValidacionException("cantidad no es un entero válido: '" + t + "'");
        }
        if (cantidad <= 0) {
            throw new ValidacionException("cantidad debe ser mayor que 0, llegó: " + cantidad);
        }
        if (cantidad > CANTIDAD_MAXIMA) {
            throw new ValidacionException("cantidad fuera de rango (máx. " + CANTIDAD_MAXIMA + "): " + cantidad);
        }
        return cantidad;
    }

    private BigDecimal validarPrecio(String texto) throws ValidacionException {
        String t = limpiar(texto);
        if (t.isEmpty()) {
            throw new ValidacionException("precio_unitario vacío");
        }
        BigDecimal precio;
        try {
            precio = new BigDecimal(t);
        } catch (NumberFormatException e) {
            throw new ValidacionException("precio_unitario no es numérico: '" + t + "'");
        }
        if (precio.signum() <= 0) {
            throw new ValidacionException("precio_unitario debe ser mayor que 0, llegó: " + t);
        }
        if (precio.compareTo(PRECIO_MAXIMO) > 0) {
            throw new ValidacionException("precio_unitario fuera de rango (máx. " + PRECIO_MAXIMO + "): " + t);
        }
        return precio.setScale(2, RoundingMode.HALF_UP);
    }

    // ---------- normalización ----------

    /** Quita espacios sobrantes (inicio, final y repetidos). Null se vuelve "". */
    private String limpiar(String texto) {
        return texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
    }

    /** "CARLOS pérez" -> "Carlos Pérez" */
    private String tituloNombre(String texto) {
        StringBuilder sb = new StringBuilder();
        for (String palabra : texto.toLowerCase(Locale.ROOT).split(" ")) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(palabra.charAt(0))).append(palabra.substring(1));
        }
        return sb.toString();
    }

    /** Igual que tituloNombre, pero respeta siglas cortas (USB, HDMI) y palabras con números (64GB). */
    private String tituloProducto(String texto) {
        StringBuilder sb = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            if (sb.length() > 0) sb.append(' ');
            boolean sigla = palabra.equals(palabra.toUpperCase(Locale.ROOT)) && palabra.length() <= 4;
            boolean tieneNumero = palabra.matches(".*\\d.*");
            if (sigla || tieneNumero) {
                sb.append(palabra);
            } else {
                String p = palabra.toLowerCase(Locale.ROOT);
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
            }
        }
        return sb.toString();
    }
}
