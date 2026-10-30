package edu.umg.ventas.lectura;

import edu.umg.ventas.modelo.RegistroCrudo;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LectorCsv implements LectorVentas {

    @Override
    public List<RegistroCrudo> leer(Path archivo) throws IOException {
        List<RegistroCrudo> registros = new ArrayList<>();
        String nombreArchivo = archivo.getFileName().toString();

        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String encabezado = lector.readLine();
            if (encabezado == null) {
                throw new IOException("El archivo CSV está vacío: " + nombreArchivo);
            }
            if (encabezado.startsWith("\uFEFF")) {
                encabezado = encabezado.substring(1);
            }
            List<String> columnas = dividirLinea(encabezado);
            for (int i = 0; i < columnas.size(); i++) {
                columnas.set(i, columnas.get(i).trim().toLowerCase());
            }

            String linea;
            int numero = 0;
            while ((linea = lector.readLine()) != null) {
                if (linea.isBlank()) {
                    continue;
                }
                numero++;
                List<String> valores = dividirLinea(linea);
                Map<String, String> campos = new LinkedHashMap<>();
                for (int i = 0; i < columnas.size(); i++) {
                    campos.put(columnas.get(i), i < valores.size() ? valores.get(i) : null);
                }
                registros.add(new RegistroCrudo(nombreArchivo, numero, campos));
            }
        }
        return registros;
    }

    private List<String> dividirLinea(String linea) {
        List<String> valores = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean entreComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                if (entreComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    actual.append('"');
                    i++;
                } else {
                    entreComillas = !entreComillas;
                }
            } else if (c == ',' && !entreComillas) {
                valores.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        valores.add(actual.toString());
        return valores;
    }
}
