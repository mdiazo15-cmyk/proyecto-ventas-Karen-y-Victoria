package edu.umg.ventas.lectura;

import edu.umg.ventas.modelo.RegistroCrudo;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface LectorVentas {
    List<RegistroCrudo> leer(Path archivo) throws IOException;
}
