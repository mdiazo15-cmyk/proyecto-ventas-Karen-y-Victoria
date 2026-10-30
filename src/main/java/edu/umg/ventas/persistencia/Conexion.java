package edu.umg.ventas.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Abre la conexión JDBC leyendo la configuración de un archivo externo (sin datos quemados). */
public class Conexion {
    private Conexion() { }

    public static Connection abrir(Path archivoConfig) throws PersistenciaException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(archivoConfig)) {
            props.load(in);
        } catch (IOException e) {
            throw new PersistenciaException(
                "No se pudo leer el archivo de configuración de BD: " + archivoConfig, e);
        }
        String url = props.getProperty("db.url");
        if (url == null || url.isBlank()) {
            throw new PersistenciaException("Falta 'db.url' en " + archivoConfig);
        }
        try {
            return DriverManager.getConnection(
                url, props.getProperty("db.user", ""), props.getProperty("db.password", ""));
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo conectar a la BD (" + url + "): " + e.getMessage(), e);
        }
    }
}
