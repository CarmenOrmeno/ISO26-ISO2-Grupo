package es.uclm.esi.iso2.cliente.controlador;

/** Lo que hace una acción con la API del servidor: recibe el token de la sesión y los valores del usuario. */
@FunctionalInterface
public interface Operacion {

    /** Devuelve el resultado que se mostrará (un objeto, una lista o un texto). */
    Object ejecutar(String token, Valores valores);
}
