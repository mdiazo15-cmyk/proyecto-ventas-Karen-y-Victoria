package edu.umg.ventas.analisis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import edu.umg.ventas.persistencia.PersistenciaException;

/** Todas las consultas SQL de los indicadores. Los cálculos los hace la BD, no Java. */
public class ConsultasAnalisis {

    // Los nombres de columna no se pueden parametrizar con '?', por eso se validan contra una lista fija.
    private static final Set<String> COLUMNAS_PERMITIDAS = Set.of("mes", "sucursal", "producto", "vendedor");

    private final Connection conexion;

    public ConsultasAnalisis(Connection conexion) {
        this.conexion = conexion;
    }

    public int contarAceptados() throws PersistenciaException {
        return contar("SELECT COUNT(*) FROM ventas", null);
    }

    public int contarRechazados(String tipo) throws PersistenciaException {
        return contar("SELECT COUNT(*) FROM registros_rechazados WHERE tipo = ?", tipo);
    }

    public BigDecimal montoTotal() throws PersistenciaException {
        return unDecimal("SELECT SUM(monto_total) FROM ventas");
    }

    public BigDecimal promedioPorTransaccion() throws PersistenciaException {
        return unDecimal("SELECT AVG(monto_total) FROM ventas");
    }

    public List<FilaAgrupada> ventasPorMes() throws PersistenciaException {
        return agrupar("mes", "clave ASC", null);
    }

    public List<FilaAgrupada> ventasPorSucursal() throws PersistenciaException {
        return agrupar("sucursal", "monto DESC, clave ASC", null);
    }

    public List<FilaAgrupada> ventasPorProducto() throws PersistenciaException {
        return agrupar("producto", "monto DESC, clave ASC", null);
    }

    public List<FilaAgrupada> ventasPorVendedor() throws PersistenciaException {
        return agrupar("vendedor", "monto DESC, clave ASC", null);
    }

    public Optional<FilaAgrupada> productoMayorMonto() throws PersistenciaException {
        return agrupar("producto", "monto DESC, clave ASC", 1).stream().findFirst();
    }

    public Optional<FilaAgrupada> productoMenorMonto() throws PersistenciaException {
        return agrupar("producto", "monto ASC, clave ASC", 1).stream().findFirst();
    }

    public List<FilaAgrupada> top3Vendedores() throws PersistenciaException {
        return agrupar("vendedor", "monto DESC, clave ASC", 3);
    }

    public Optional<TransaccionResumen> transaccionMayor() throws PersistenciaException {
        return transaccionExtrema("DESC");
    }

    public Optional<TransaccionResumen> transaccionMenor() throws PersistenciaException {
        return transaccionExtrema("ASC");
    }

    // ---------- métodos privados ----------

    private int contar(String sql, String parametro) throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setString(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error ejecutando conteo: " + sql, e);
        }
    }

    private BigDecimal unDecimal(String sql) throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            BigDecimal valor = rs.next() ? rs.getBigDecimal(1) : null;
            return redondear(valor);
        } catch (SQLException e) {
            throw new PersistenciaException("Error ejecutando consulta: " + sql, e);
        }
    }

    private List<FilaAgrupada> agrupar(String columna, String orden, Integer limite)
            throws PersistenciaException {
        if (!COLUMNAS_PERMITIDAS.contains(columna)) {
            throw new IllegalArgumentException("Columna no permitida para agrupar: " + columna);
        }
        String sql = "SELECT " + columna + " AS clave, COUNT(*) AS n, SUM(monto_total) AS monto "
                   + "FROM ventas GROUP BY " + columna + " ORDER BY " + orden
                   + (limite != null ? " LIMIT ?" : "");
        List<FilaAgrupada> filas = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            if (limite != null) {
                ps.setInt(1, limite);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new FilaAgrupada(rs.getString("clave"), rs.getInt("n"),
                                               redondear(rs.getBigDecimal("monto"))));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error agrupando ventas por " + columna, e);
        }
        return filas;
    }

    private Optional<TransaccionResumen> transaccionExtrema(String direccion) throws PersistenciaException {
        // 'direccion' solo toma valores fijos del código (ASC/DESC), nunca datos externos.
        String sql = "SELECT id_venta, fecha, sucursal, producto, vendedor, monto_total "
                   + "FROM ventas ORDER BY monto_total " + direccion + ", id_venta ASC LIMIT 1";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(new TransaccionResumen(
                    rs.getString("id_venta"), rs.getString("fecha"), rs.getString("sucursal"),
                    rs.getString("producto"), rs.getString("vendedor"),
                    redondear(rs.getBigDecimal("monto_total"))));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new PersistenciaException("Error buscando transacción " + direccion, e);
        }
    }

    private BigDecimal redondear(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor.setScale(2, RoundingMode.HALF_UP);
    }
}
