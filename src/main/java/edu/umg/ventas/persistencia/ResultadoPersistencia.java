package edu.umg.ventas.persistencia;

import java.util.List;

/**
 * Resumen de lo ocurrido al guardar.
 * yaExistentes = ventas cuyo id ya estaba en la BD (reproceso); no es error.
 */
public record ResultadoPersistencia(int insertadas, int yaExistentes,
                                    int rechazadosGuardados, List<String> fallos) { }
