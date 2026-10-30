package edu.umg.ventas.persistencia;

/** Error de base de datos que impide continuar (conexión, configuración, consulta). */
public class PersistenciaException extends Exception {
    public PersistenciaException(String mensaje, Throwable causa) { super(mensaje, causa); }
    public PersistenciaException(String mensaje) { super(mensaje); }
}
