package edu.umg.ventas.persistencia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Crea las tablas ejecutando schema.sql (empaquetado en resources). Es seguro repetirlo: usa IF NOT EXISTS. */
public class InicializadorBD {
    private static final String RECURSO = "/schema.sql";

    private InicializadorBD() { }

    public static void crearTablas(Connection conexion) throws PersistenciaException {
        String script = leerScript();
        try (Statement st = conexion.createStatement()) {
            for (String sentencia : script.split(";")) {
                if (!sentencia.isBlank()) {
                    st.execute(sentencia);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error creando las tablas: " + e.getMessage(), e);
        }
    }

    private static String leerScript() throws PersistenciaException {
        try (InputStream in = InicializadorBD.class.getResourceAsStream(RECURSO)) {
            if (in == null) {
                throw new PersistenciaException("No se encontró " + RECURSO + " en resources");
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    if (!linea.trim().startsWith("--")) { // se ignoran los comentarios
                        sb.append(linea).append('\n');
                    }
                }
            }
            return sb.toString();
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer " + RECURSO, e);
        }
    }
}
