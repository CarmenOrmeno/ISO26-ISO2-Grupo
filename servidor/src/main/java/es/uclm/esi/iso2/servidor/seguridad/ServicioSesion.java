package es.uclm.esi.iso2.servidor.seguridad;

/** Contrato interno del componente de sesiones (CU01). */
public interface ServicioSesion {

    /**
     * Autentica contra el SSO y abre una sesión.
     *
     * @throws es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException si las credenciales no son
     *         válidas o el usuario no está dado de alta en la aplicación
     */
    Sesion iniciarSesion(String login, String contrasena);

    void cerrarSesion(String token);

    /** Usuario de una sesión válida; renueva la sesión. Lanza AccesoDenegadoException si no es válida. */
    Usuario usuarioDe(String token);

    /** Número total de sesiones iniciadas desde el arranque (uso del sistema). */
    long sesionesIniciadas();
}
