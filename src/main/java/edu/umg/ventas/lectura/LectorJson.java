package edu.umg.ventas.lectura;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
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

public class LectorJson implements LectorVentas {

    @Override
    public List<RegistroCrudo> leer(Path archivo) throws IOException {
        String nombreArchivo = archivo.getFileName().toString();
        List<RegistroCrudo> registros = new ArrayList<>();

        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            JsonElement raiz = JsonParser.parseReader(lector);
            if (!raiz.isJsonArray()) {
                throw new IOException("El JSON debe ser un arreglo de objetos: " + nombreArchivo);
            }
            JsonArray arreglo = raiz.getAsJsonArray();
            int numero = 0;
            for (JsonElement elemento : arreglo) {
                numero++;
                registros.add(new RegistroCrudo(nombreArchivo, numero, convertirElemento(elemento)));
            }
        } catch (JsonParseException e) {
            throw new IOException("JSON mal formado en " + nombreArchivo + ": " + e.getMessage(), e);
        }
        return registros;
    }

    /** Convierte un objeto JSON en un mapa campo -> texto. Si no es objeto, devuelve un mapa vacío. */
    private Map<String, String> convertirElemento(JsonElement elemento) {
        Map<String, String> campos = new LinkedHashMap<>();
        if (!elemento.isJsonObject()) {
            return campos;   // el validador lo rechazará por campos vacíos
        }
        JsonObject objeto = elemento.getAsJsonObject();
        for (String clave : objeto.keySet()) {
            campos.put(clave.trim().toLowerCase(), valorComoTexto(objeto.get(clave)));
        }
        return campos;
    }

    /** Nunca lanza excepción: un valor raro (objeto, arreglo) llega como texto y se rechaza al validar. */
    private String valorComoTexto(JsonElement valor) {
        if (valor == null || valor.isJsonNull()) {
            return null;
        }
        return valor.isJsonPrimitive() ? valor.getAsString() : valor.toString();
    }
}
