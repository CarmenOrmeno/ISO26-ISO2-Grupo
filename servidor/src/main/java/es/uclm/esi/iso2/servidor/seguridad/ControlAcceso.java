package es.uclm.esi.iso2.servidor.seguridad;

import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;

/** Punto único de autorización de la API: sesión válida + permiso (RNF-S1). */
public class ControlAcceso {

    private final ServicioSesion sesiones;
    private final PoliticaPermisos politica;

    public ControlAcceso(ServicioSesion sesiones, PoliticaPermisos politica) {
        this.sesiones = sesiones;
        this.politica = politica;
    }

    /** Devuelve el usuario de la sesión si tiene el permiso; si no, lanza AccesoDenegadoException. */
    public Usuario exigir(String token, Permiso permiso) {
        Usuario usuario = sesiones.usuarioDe(token);
        if (!politica.tiene(usuario.rol(), permiso)) {
            throw new AccesoDenegadoException("El rol " + usuario.rol() + " no tiene el permiso " + permiso);
        }
        return usuario;
    }
}
