package edu.umg.ventas.analisis;

import java.math.BigDecimal;

public record TransaccionResumen(String idVenta, String fecha, String sucursal,
                                 String producto, String vendedor, BigDecimal monto) { }
