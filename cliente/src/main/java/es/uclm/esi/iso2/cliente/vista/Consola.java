package es.uclm.esi.iso2.cliente.vista;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Vista de consola (MVC). Solo lee y escribe: no conoce el servidor ni tiene lógica de negocio.
 * Cuando se acaba la entrada lanza {@link FinDeEntradaException}.
 */
public class Consola {

    private final BufferedReader entrada;
    private final PrintStream salida;

    public Consola(BufferedReader entrada, PrintStream salida) {
        this.entrada = entrada;
        this.salida = salida;
    }

    public void mostrar(String texto) {
        salida.println(texto);
    }

    public void titulo(String texto) {
        salida.println();
        salida.println("== " + texto + " ==");
    }

    public void error(String texto) {
        salida.println("ERROR: " + texto);
    }

    public String leer(String etiqueta) {
        salida.print(etiqueta + ": ");
        try {
            String linea = entrada.readLine();
            if (linea == null) {
                throw new FinDeEntradaException();
            }
            return linea.strip();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Lee un entero; repite la pregunta hasta que el valor sea válido. */
    public int leerEntero(String etiqueta) {
        while (true) {
            String texto = leer(etiqueta);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                error("«" + texto + "» no es un número");
            }
        }
    }

    public long leerLargo(String etiqueta) {
        return leerEntero(etiqueta);
    }

    /** Lee una fecha con formato AAAA-MM-DD; repite hasta que sea válida. */
    public LocalDate leerFecha(String etiqueta) {
        while (true) {
            String texto = leer(etiqueta + " (AAAA-MM-DD)");
            try {
                return LocalDate.parse(texto);
            } catch (DateTimeParseException e) {
                error("«" + texto + "» no es una fecha válida");
            }
        }
    }

    /** Lee una hora con formato HH:MM; repite hasta que sea válida. */
    public LocalTime leerHora(String etiqueta) {
        while (true) {
            String texto = leer(etiqueta + " (HH:MM)");
            try {
                return LocalTime.parse(texto);
            } catch (DateTimeParseException e) {
                error("«" + texto + "» no es una hora válida");
            }
        }
    }

    public boolean confirmar(String pregunta) {
        return leer(pregunta + " (s/n)").equalsIgnoreCase("s");
    }

    /**
     * Muestra un menú numerado desde 1 y la opción 0 para volver. Devuelve la opción elegida
     * (0 = volver), repitiendo la pregunta si está fuera de rango.
     */
    public int elegir(String titulo, List<String> opciones) {
        titulo(titulo);
        for (int i = 0; i < opciones.size(); i++) {
            salida.println((i + 1) + ". " + opciones.get(i));
        }
        salida.println("0. Volver / salir");
        while (true) {
            int opcion = leerEntero("Opción");
            if (opcion >= 0 && opcion <= opciones.size()) {
                return opcion;
            }
            error("Opción fuera de rango");
        }
    }

    public <T> void listar(String titulo, List<T> elementos) {
        titulo(titulo);
        if (elementos.isEmpty()) {
            salida.println("(sin resultados)");
        }
        elementos.forEach(e -> salida.println("- " + e));
    }
}
