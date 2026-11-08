package es.uclm.esi.iso2.cliente.vista;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/** Consola de pruebas: entrada guionizada (una línea por respuesta) y salida capturada. */
public class Guion {

    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();
    private final Consola consola;

    public Guion(String... lineas) {
        String texto = lineas.length == 0 ? "" : String.join("\n", lineas) + "\n";
        consola = new Consola(new BufferedReader(new StringReader(texto)),
                new PrintStream(salida, true, StandardCharsets.UTF_8));
    }

    public Consola consola() {
        return consola;
    }

    public String salida() {
        return salida.toString(StandardCharsets.UTF_8);
    }
}
