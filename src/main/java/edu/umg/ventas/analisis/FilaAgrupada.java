package edu.umg.ventas.analisis;

import java.math.BigDecimal;

/** Una fila de un GROUP BY: clave (mes, sucursal, producto o vendedor), # de transacciones y monto. */
public record FilaAgrupada(String clave, int transacciones, BigDecimal monto) { }
