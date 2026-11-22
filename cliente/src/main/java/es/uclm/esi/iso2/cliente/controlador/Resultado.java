package es.uclm.esi.iso2.cliente.controlador;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Lo que la vista muestra tras ejecutar una acción: si fue bien, un mensaje y las líneas del resultado.
 * Los errores (del formato de un campo o del servidor) se convierten en un resultado incorrecto, no en una
 * excepción, para que la vista siga funcionando.
 */
public record Resultado(boolean correcto, String mensaje, List<String> lineas) {

    public Resultado {
        lineas = List.copyOf(lineas);
    }

    public static Resultado de(Object valor) {
        if (valor == null) {
            return new Resultado(true, "Hecho", List.of());
        }
        if (valor instanceof Collection<?> elementos) {
            String mensaje = elementos.isEmpty() ? "Sin resultados"
                    : elementos.size() == 1 ? "1 resultado" : elementos.size() + " resultados";
            return new Resultado(true, mensaje, elementos.stream().map(String::valueOf).toList());
        }
        if (valor instanceof String texto) {
            return new Resultado(true, texto.lines().findFirst().orElse(""), texto.lines().skip(1).toList());
        }
        return new Resultado(true, "Correcto", Arrays.asList(valor.toString().split("\n")));
    }

    public static Resultado error(RuntimeException e) {
        String mensaje = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        return new Resultado(false, mensaje, List.of());
    }
}
