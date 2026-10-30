package edu.umg.ventas.lectura;

import edu.umg.ventas.modelo.RegistroCrudo;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class LectorXml implements LectorVentas {

    @Override
    public List<RegistroCrudo> leer(Path archivo) throws IOException {
        String nombreArchivo = archivo.getFileName().toString();
        List<RegistroCrudo> registros = new ArrayList<>();

        try (InputStream in = Files.newInputStream(archivo)) {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = fabrica.newDocumentBuilder();
            Document doc = builder.parse(in);

            NodeList ventas = doc.getDocumentElement().getElementsByTagName("venta");
            for (int i = 0; i < ventas.getLength(); i++) {
                Element venta = (Element) ventas.item(i);
                Map<String, String> campos = new LinkedHashMap<>();
                NodeList hijos = venta.getChildNodes();
                for (int j = 0; j < hijos.getLength(); j++) {
                    Node hijo = hijos.item(j);
                    if (hijo.getNodeType() == Node.ELEMENT_NODE) {
                        campos.put(hijo.getNodeName().trim().toLowerCase(), hijo.getTextContent());
                    }
                }
                registros.add(new RegistroCrudo(nombreArchivo, i + 1, campos));
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("XML mal formado en " + nombreArchivo + ": " + e.getMessage(), e);
        }
        return registros;
    }
}
