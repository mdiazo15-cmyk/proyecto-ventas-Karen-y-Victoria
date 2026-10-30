package edu.umg.ventas.reporte;

import edu.umg.ventas.modelo.RegistroRechazado;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Genera errores_procesamiento.txt con cada registro rechazado y su motivo. */
public class EscritorErrores {

    public void generar(Path destino, List<RegistroRechazado> rechazados) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            w.write("ERRORES DE PROCESAMIENTO");
            w.newLine();
            w.write("=".repeat(60));
            w.newLine();
            w.write("Total de registros rechazados: " + rechazados.size());
            w.newLine();
            w.newLine();
            for (RegistroRechazado r : rechazados) {
                String tipo = r.isDuplicado() ? "DUPLICADO" : "INVALIDO";
                String id = r.getIdVenta() == null ? "(sin id)" : r.getIdVenta();
                w.write("[" + tipo + "] archivo=" + r.getArchivo()
                        + " | registro #" + r.getNumeroRegistro()
                        + " | id_venta=" + id
                        + " | motivo: " + r.getMotivo());
                w.newLine();
            }
        }
    }
}