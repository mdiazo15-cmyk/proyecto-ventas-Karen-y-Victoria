package edu.umg.ventas.validacion;

import edu.umg.ventas.modelo.RegistroRechazado;
import edu.umg.ventas.modelo.Venta;
import java.util.List;

public record ResultadoValidacion(List<Venta> validas, List<RegistroRechazado> rechazados) { }
