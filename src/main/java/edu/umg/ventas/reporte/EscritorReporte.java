package edu.umg.ventas.reporte;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import edu.umg.ventas.analisis.ConsultasAnalisis;
import edu.umg.ventas.analisis.FilaAgrupada;
import edu.umg.ventas.analisis.TransaccionResumen;
import edu.umg.ventas.persistencia.PersistenciaException;

/** Genera reporte_analisis.txt a partir de las consultas a la BD. */
public class EscritorReporte {

    private final ConsultasAnalisis consultas;

    public EscritorReporte(ConsultasAnalisis consultas) {
        this.consultas = consultas;
    }

    public void generar(Path destino) throws PersistenciaException, IOException {
        // Se consulta todo primero: si la BD falla, no queda un archivo a medias.
        int aceptados = consultas.contarAceptados();
        int invalidos = consultas.contarRechazados("INVALIDO");
        int duplicados = consultas.contarRechazados("DUPLICADO");
        int recibidos = aceptados + invalidos + duplicados;
        BigDecimal total = consultas.montoTotal();
        BigDecimal promedio = consultas.promedioPorTransaccion();
        List<FilaAgrupada> porMes = consultas.ventasPorMes();
        List<FilaAgrupada> porSucursal = consultas.ventasPorSucursal();
        List<FilaAgrupada> porProducto = consultas.ventasPorProducto();
        List<FilaAgrupada> porVendedor = consultas.ventasPorVendedor();
        Optional<FilaAgrupada> prodMax = consultas.productoMayorMonto();
        Optional<FilaAgrupada> prodMin = consultas.productoMenorMonto();
        Optional<TransaccionResumen> trMax = consultas.transaccionMayor();
        Optional<TransaccionResumen> trMin = consultas.transaccionMenor();
        List<FilaAgrupada> top3 = consultas.top3Vendedores();

        try (BufferedWriter w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            linea(w, "REPORTE DE ANÁLISIS DE VENTAS");
            linea(w, "=".repeat(60));

            linea(w, "");
            linea(w, "1. CONTEO DE REGISTROS");
            linea(w, "Recibidos : " + recibidos);
            linea(w, "Aceptados : " + aceptados);
            linea(w, "Rechazados (inválidos): " + invalidos);
            linea(w, "Duplicados: " + duplicados);
            linea(w, "% rechazo (solo inválidos)      : " + porcentaje(invalidos, recibidos));
            linea(w, "% rechazo (inválidos+duplicados): " + porcentaje(invalidos + duplicados, recibidos));

            linea(w, "");
            linea(w, "2. MONTOS GENERALES");
            linea(w, "Monto total vendido      : " + dinero(total));
            linea(w, "Promedio por transacción : " + dinero(promedio));

            escribirTabla(w, "3. VENTAS POR MES", "Mes", porMes);
            escribirTabla(w, "4. VENTAS POR SUCURSAL", "Sucursal", porSucursal);
            escribirTabla(w, "5. VENTAS POR PRODUCTO", "Producto", porProducto);
            escribirTabla(w, "6. VENTAS POR VENDEDOR", "Vendedor", porVendedor);

            linea(w, "");
            linea(w, "7. PRODUCTOS EXTREMOS");
            linea(w, "Mayor monto: " + describir(prodMax));
            linea(w, "Menor monto: " + describir(prodMin));

            linea(w, "");
            linea(w, "8. TRANSACCIONES EXTREMAS");
            linea(w, "Mayor monto: " + describir(trMax));
            linea(w, "Menor monto: " + describir(trMin));

            escribirTabla(w, "9. TOP 3 VENDEDORES POR MONTO", "Vendedor", top3);
        }
    }

    private void escribirTabla(BufferedWriter w, String titulo, String columna,
                               List<FilaAgrupada> filas) throws IOException {
        linea(w, "");
        linea(w, titulo);
        linea(w, String.format(Locale.US, "%-30s %10s %16s", columna, "Trans.", "Monto"));
        for (FilaAgrupada f : filas) {
            linea(w, String.format(Locale.US, "%-30s %10d %16s", f.clave(), f.transacciones(), dinero(f.monto())));
        }
    }

    private String describir(Optional<?> valor) {
        if (valor.isEmpty()) {
            return "sin datos";
        }
        Object o = valor.get();
        if (o instanceof FilaAgrupada f) {
            return f.clave() + " -> " + dinero(f.monto()) + " (" + f.transacciones() + " transacciones)";
        }
        if (o instanceof TransaccionResumen t) {
            return t.idVenta() + " | " + t.fecha() + " | " + t.sucursal() + " | " + t.producto()
                 + " | " + t.vendedor() + " -> " + dinero(t.monto());
        }
        return o.toString();
    }

    private String porcentaje(int parte, int total) {
        if (total == 0) {
            return "0.00%";
        }
        BigDecimal p = BigDecimal.valueOf(parte * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
        return p + "%";
    }

    private String dinero(BigDecimal valor) {
        return String.format(Locale.US, "%,.2f", valor);
    }

    private void linea(BufferedWriter w, String texto) throws IOException {
        w.write(texto);
        w.newLine();
    }
}
