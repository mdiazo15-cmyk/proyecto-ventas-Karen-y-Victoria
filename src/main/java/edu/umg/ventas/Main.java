package edu.umg.ventas;

import edu.umg.ventas.lectura.LectorCsv;
import edu.umg.ventas.modelo.RegistroCrudo;
import edu.umg.ventas.modelo.RegistroRechazado;
import edu.umg.ventas.modelo.Venta;
import edu.umg.ventas.validacion.ResultadoValidacion;
import edu.umg.ventas.validacion.ValidadorVenta;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        List<RegistroCrudo> crudos = new LectorCsv().leer(Path.of("datos/ventas.csv"));
        ResultadoValidacion res = new ValidadorVenta().procesar(crudos);

        System.out.println("VÁLIDAS: " + res.validas().size());
        for (Venta v : res.validas()) {
            System.out.println(v.getIdVenta() + " | " + v.getFecha() + " | " + v.getSucursal()
                + " | " + v.getProducto() + " | " + v.getVendedor() + " | " + v.getMonto());
        }
        System.out.println("RECHAZADAS: " + res.rechazados().size());
        for (RegistroRechazado r : res.rechazados()) {
            System.out.println(r.getArchivo() + " #" + r.getNumeroRegistro() + " ["
                + (r.isDuplicado() ? "DUPLICADO" : "INVÁLIDO") + "] " + r.getMotivo());
        }
    }
}
