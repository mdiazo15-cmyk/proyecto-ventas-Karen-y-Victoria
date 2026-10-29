package edu.umg.ventas.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Venta {
    private final String idVenta;
    private final LocalDate fecha;
    private final String sucursal;
    private final String codigoProducto;
    private final String producto;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final String vendedor;
    private final String archivoOrigen;

    public Venta(String idVenta, LocalDate fecha, String sucursal, String codigoProducto,
                 String producto, int cantidad, BigDecimal precioUnitario,
                 String vendedor, String archivoOrigen) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.sucursal = sucursal;
        this.codigoProducto = codigoProducto;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.vendedor = vendedor;
        this.archivoOrigen = archivoOrigen;
    }

    public BigDecimal getMonto() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public String getIdVenta() { return idVenta; }
    public LocalDate getFecha() { return fecha; }
    public String getSucursal() { return sucursal; }
    public String getCodigoProducto() { return codigoProducto; }
    public String getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public String getVendedor() { return vendedor; }
    public String getArchivoOrigen() { return archivoOrigen; }
}
