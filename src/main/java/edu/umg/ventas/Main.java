package edu.umg.ventas;

import edu.umg.ventas.analisis.ConsultasAnalisis;
import edu.umg.ventas.lectura.LectorCsv;
import edu.umg.ventas.lectura.LectorVentas;
import edu.umg.ventas.modelo.RegistroCrudo;
import edu.umg.ventas.modelo.RegistroRechazado;
import edu.umg.ventas.modelo.Venta;
import edu.umg.ventas.persistencia.Conexion;
import edu.umg.ventas.persistencia.InicializadorBD;
import edu.umg.ventas.persistencia.PersistenciaException;
import edu.umg.ventas.persistencia.ResultadoPersistencia;
import edu.umg.ventas.persistencia.VentaDAO;
import edu.umg.ventas.reporte.EscritorErrores;
import edu.umg.ventas.reporte.EscritorReporte;
import edu.umg.ventas.validacion.ResultadoValidacion;
import edu.umg.ventas.validacion.ValidadorVenta;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final Path CONFIG_BD = Path.of("config", "db.properties");
    private static final Path CARPETA_DATOS = Path.of("datos");
    private static final Path CARPETA_SALIDA = Path.of("salida");

    public static void main(String[] args) {
        System.out.println("=== Sistema de Integración y Análisis de Datos de Ventas ===");

        // 1. Leer y validar (no necesita base de datos)
        List<RegistroCrudo> crudos = leerArchivos();
        ValidadorVenta validador = new ValidadorVenta();
        ResultadoValidacion resultado = validador.procesar(crudos);
        System.out.println("Recibidos: " + crudos.size()
                + " | válidos: " + resultado.validas().size()
                + " | rechazados: " + resultado.rechazados().size());

        // 2. Guardar y analizar en la base de datos
        try (Connection conexion = Conexion.abrir(CONFIG_BD)) {
            InicializadorBD.crearTablas(conexion);
            guardar(conexion, resultado.validas(), resultado.rechazados());
            generarReportes(conexion, resultado.rechazados());
        } catch (PersistenciaException e) {
            System.err.println("Error de base de datos: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    /** Lee todos los archivos CSV, JSON y XML de la carpeta datos. */
    private static List<RegistroCrudo> leerArchivos() {
        List<RegistroCrudo> todos = new ArrayList<>();
        try (var archivos = java.nio.file.Files.list(CARPETA_DATOS)) {
            List<Path> lista = archivos.sorted().toList();
            for (Path archivo : lista) {
                LectorVentas lector = elegirLector(archivo);
                if (lector == null) {
                    continue; // extensión no soportada: se ignora
                }
                try {
                    todos.addAll(lector.leer(archivo));
                } catch (IOException e) {
                    System.err.println("No se pudo leer " + archivo.getFileName() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo listar la carpeta " + CARPETA_DATOS + ": " + e.getMessage());
        }
        return todos;
    }

    /** Elige el lector según la extensión. Para un formato nuevo solo se agrega un caso. */
    private static LectorVentas elegirLector(Path archivo) {
        String nombre = archivo.getFileName().toString().toLowerCase();
        if (nombre.endsWith(".csv")) {
            return new LectorCsv();
        }
        // Aquí se agregarán .json y .xml cuando existan LectorJson y LectorXml
        return null;
    }

    private static void guardar(Connection conexion, List<Venta> validas,
                                List<RegistroRechazado> rechazados) throws PersistenciaException {
        ResultadoPersistencia r = new VentaDAO(conexion).guardar(validas, rechazados);
        System.out.println("Insertadas: " + r.insertadas()
                + " | ya existentes (reproceso): " + r.yaExistentes()
                + " | rechazos guardados: " + r.rechazadosGuardados());
        for (String fallo : r.fallos()) {
            System.err.println("Fallo al guardar: " + fallo);
        }
    }

    private static void generarReportes(Connection conexion, List<RegistroRechazado> rechazados)
            throws PersistenciaException {
        try {
            java.nio.file.Files.createDirectories(CARPETA_SALIDA);
            new EscritorErrores().generar(CARPETA_SALIDA.resolve("errores_procesamiento.txt"), rechazados);
            new EscritorReporte(new ConsultasAnalisis(conexion))
                    .generar(CARPETA_SALIDA.resolve("reporte_analisis.txt"));
            System.out.println("Reportes generados en la carpeta: " + CARPETA_SALIDA);
        } catch (IOException e) {
            System.err.println("No se pudieron escribir los reportes: " + e.getMessage());
        }
    }
}
