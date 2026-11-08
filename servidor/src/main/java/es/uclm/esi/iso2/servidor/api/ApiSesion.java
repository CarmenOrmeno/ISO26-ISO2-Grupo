package es.uclm.esi.iso2.servidor.api;

import es.uclm.esi.iso2.servidor.seguridad.Usuario;

/** CU01 · Iniciar sesión. Contrato con el cliente. */
public interface ApiSesion {

    /** Devuelve el token de la sesión abierta. */
    String iniciarSesion(String login, String contrasena);

    void cerrarSesion(String token);

    Usuario usuarioActual(String token);
}
