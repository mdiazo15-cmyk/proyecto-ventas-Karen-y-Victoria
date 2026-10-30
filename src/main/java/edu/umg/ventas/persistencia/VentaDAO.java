package edu.umg.ventas.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import edu.umg.ventas.modelo.RegistroRechazado;
import edu.umg.ventas.modelo.Venta;

/**
 * Guarda ventas válidas y registros rechazados.
 * Idempotente: antes de insertar consulta si la clave ya existe; si existe, no inserta.
 * Un fallo en un registro NO detiene los demás.
 */
public class VentaDAO {

    private static final String EXISTE_VENTA = "SELECT 1 FROM ventas WHERE id_venta = ?";
    private static final String INSERTAR_VENTA =
        "INSERT INTO ventas (id_venta, fecha, mes, sucursal, codigo_producto, producto, "
      + "cantidad, precio_unitario, monto_total, vendedor, archivo_origen) "
      + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String EXISTE_RECHAZO =
        "SELECT 1 FROM registros_rechazados WHERE archivo_origen = ? AND numero_registro = ?";
    private static final String INSERTAR_RECHAZO =
        "INSERT INTO registros_rechazados (archivo_origen, numero_registro, tipo, id_venta, motivo) "
      + "VALUES (?, ?, ?, ?, ?)";

    private final Connection conexion;

    public VentaDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public ResultadoPersistencia guardar(List<Venta> ventas, List<RegistroRechazado> rechazados)
            throws PersistenciaException {
        List<String> fallos = new ArrayList<>();
        int insertadas = 0;
        int yaExistentes = 0;
        int rechazosGuardados = 0;

        try (PreparedStatement existe = conexion.prepareStatement(EXISTE_VENTA);
             PreparedStatement insertar = conexion.prepareStatement(INSERTAR_VENTA)) {
            for (Venta v : ventas) {
                try {
                    if (existeClave(existe, v.getIdVenta())) {
                        yaExistentes++;
                        continue;
                    }
                    llenarInsertVenta(insertar, v);
                    insertar.executeUpdate();
                    insertadas++;
                } catch (SQLException e) {
                    fallos.add("Venta " + v.getIdVenta() + " (archivo " + v.getArchivoOrigen()
                               + "): error de BD: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron preparar las sentencias de ventas", e);
        }

        try (PreparedStatement existe = conexion.prepareStatement(EXISTE_RECHAZO);
             PreparedStatement insertar = conexion.prepareStatement(INSERTAR_RECHAZO)) {
            for (RegistroRechazado r : rechazados) {
                try {
                    existe.setString(1, r.getArchivo());
                    existe.setInt(2, r.getNumeroRegistro());
                    try (ResultSet rs = existe.executeQuery()) {
                        if (rs.next()) {
                            continue; // ya registrado en una ejecución anterior
                        }
                    }
                    insertar.setString(1, r.getArchivo());
                    insertar.setInt(2, r.getNumeroRegistro());
                    insertar.setString(3, (r.isDuplicado() ? "DUPLICADO" : "INVALIDO"));
                    insertar.setString(4, r.getIdVenta());
                    insertar.setString(5, r.getMotivo());
                    insertar.executeUpdate();
                    rechazosGuardados++;
                } catch (SQLException e) {
                    fallos.add("Rechazo " + r.getArchivo() + " registro " + r.getNumeroRegistro()
                               + ": error de BD: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron preparar las sentencias de rechazados", e);
        }

        return new ResultadoPersistencia(insertadas, yaExistentes, rechazosGuardados, fallos);
    }

    private boolean existeClave(PreparedStatement existe, String id) throws SQLException {
        existe.setString(1, id);
        try (ResultSet rs = existe.executeQuery()) {
            return rs.next();
        }
    }

    private void llenarInsertVenta(PreparedStatement ps, Venta v) throws SQLException {
        ps.setString(1, v.getIdVenta());
        ps.setString(2, v.getFecha().toString());
        ps.setString(3, YearMonth.from(v.getFecha()).toString());
        ps.setString(4, v.getSucursal());
        ps.setString(5, v.getCodigoProducto());
        ps.setString(6, v.getProducto());
        ps.setInt(7, v.getCantidad());
        ps.setBigDecimal(8, v.getPrecioUnitario());
        ps.setBigDecimal(9, v.getMonto());
        ps.setString(10, v.getVendedor());
        ps.setString(11, v.getArchivoOrigen());
    }
}
