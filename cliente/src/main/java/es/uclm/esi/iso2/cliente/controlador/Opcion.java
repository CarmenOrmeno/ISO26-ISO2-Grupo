package es.uclm.esi.iso2.cliente.controlador;

import java.util.function.Consumer;

/** Una opción de menú: texto y acción que recibe el token de la sesión. */
public record Opcion(String texto, Consumer<String> accion) {
}
