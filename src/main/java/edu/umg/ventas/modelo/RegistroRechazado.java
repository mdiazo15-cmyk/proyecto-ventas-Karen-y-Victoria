package edu.umg.ventas.modelo;

public class RegistroRechazado {
    private final String archivo;
    private final int numeroRegistro;
    private final String idVenta;      // puede ser null si venía vacío
    private final String motivo;
    private final boolean duplicado;   // true si es duplicado, false si es inválido

    public RegistroRechazado(String archivo, int numeroRegistro, String idVenta,
                             String motivo, boolean duplicado) {
        this.archivo = archivo;
        this.numeroRegistro = numeroRegistro;
        this.idVenta = idVenta;
        this.motivo = motivo;
        this.duplicado = duplicado;
    }

    public String getArchivo() { return archivo; }
    public int getNumeroRegistro() { return numeroRegistro; }
    public String getIdVenta() { return idVenta; }
    public String getMotivo() { return motivo; }
    public boolean isDuplicado() { return duplicado; }
}
