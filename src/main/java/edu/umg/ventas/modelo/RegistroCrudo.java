package edu.umg.ventas.modelo;

import java.util.Map;

public class RegistroCrudo {
    private final String archivo;
    private final int numero;
    private final Map<String, String> campos;

    public RegistroCrudo(String archivo, int numero, Map<String, String> campos) {
        this.archivo = archivo;
        this.numero = numero;
        this.campos = campos;
    }

    public String getArchivo() { return archivo; }
    public int getNumero() { return numero; }
    public Map<String, String> getCampos() { return campos; }
    public String getCampo(String nombre) { return campos.get(nombre); }
}
