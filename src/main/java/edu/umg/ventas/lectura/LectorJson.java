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
                Map<String, String> campos = new LinkedHashMap<>();
                if (elemento.isJsonObject()) {
                    JsonObject obj = elemento.getAsJsonObject();
                    for (String clave : obj.keySet()) {
                        JsonElement valor = obj.get(clave);
                        campos.put(clave.trim().toLowerCase(),
                                valor == null || valor.isJsonNull() ? null : valor.getAsString());
                    }
                }
                registros.add(new RegistroCrudo(nombreArchivo, numero, campos));
            }
        } catch (JsonParseException e) {
            throw new IOException("JSON mal formado en " + nombreArchivo + ": " + e.getMessage(), e);
        }
        return registros;
    }
}
