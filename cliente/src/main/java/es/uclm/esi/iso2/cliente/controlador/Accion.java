package es.uclm.esi.iso2.cliente.controlador;

import java.util.List;

/**
 * Una operación que el usuario puede hacer desde el cliente (MVC: es el controlador de esa pantalla). La vista
 * pide los {@link #campos()} y llama a {@link #ejecutar}.
 *
 * @param grupo caso de uso o área al que pertenece (p. ej. «CU07 Mis citas»), para agrupar en la vista
 */
public record Accion(String grupo, String nombre, List<Campo> campos, Operacion operacion) {

    public Accion {
        campos = List.copyOf(campos);
    }

    /** Acción que devuelve un resultado. */
    public static Accion de(String grupo, String nombre, Operacion operacion, Campo... campos) {
        return new Accion(grupo, nombre, List.of(campos), operacion);
    }

    /** Orden sin resultado: muestra {@code mensaje} si va bien. */
    public static Accion orden(String grupo, String nombre, String mensaje, Orden orden, Campo... campos) {
        return new Accion(grupo, nombre, List.of(campos), (token, v) -> {
            orden.ejecutar(token, v);
            return mensaje;
        });
    }

    /** Operación de la API que no devuelve nada. */
    @FunctionalInterface
    public interface Orden {
        void ejecutar(String token, Valores valores);
    }

    /** Interpreta los textos y llama al servidor; cualquier error se devuelve como resultado incorrecto. */
    public Resultado ejecutar(String token, List<String> textos) {
        try {
            return Resultado.de(operacion.ejecutar(token, Valores.de(campos, textos)));
        } catch (RuntimeException e) {
            return Resultado.error(e);
        }
    }

    @Override
    public String toString() {
        return nombre;
    }
}
