package es.uclm.esi.iso2.servidor.seguridad;

import java.util.Objects;

import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;

/**
 * Punto único de autorización de la API: sesión válida + permiso (RNF-S1). Deja constancia en la auditoría
 * de los accesos a datos clínicos y de todos los accesos denegados por falta de permiso (RNF-S2, CU18).
 */
public class ControlAcceso {

    private final ServicioSesion sesiones;
    private final ConsultaPermisos politica;
    private final Auditor auditor;

    /** El auditor es obligatorio: así no se puede montar un control de acceso que no audite (RNF-S2). */
    public ControlAcceso(ServicioSesion sesiones, ConsultaPermisos politica, Auditor auditor) {
        this.sesiones = sesiones;
        this.politica = politica;
        this.auditor = Objects.requireNonNull(auditor, "auditor");
    }

    /** Devuelve el usuario de la sesión si tiene el permiso; si no, lanza AccesoDenegadoException. */
    public Usuario exigir(String token, Permiso permiso) {
        return exigir(token, permiso, "");
    }

    /**
     * Igual que {@link #exigir(String, Permiso)}, indicando el recurso al que se accede para la auditoría.
     * Una sesión no válida no se audita: no hay usuario al que atribuir el intento.
     */
    public Usuario exigir(String token, Permiso permiso, String recurso) {
        Usuario usuario = sesiones.usuarioDe(token);
        boolean permitido = politica.tiene(usuario.rol(), permiso);
        if (!permitido || permiso.accedeADatosClinicos()) {
            auditor.registrar(usuario, permiso, recurso == null ? "" : recurso, permitido);
        }
        if (!permitido) {
            throw new AccesoDenegadoException("El rol " + usuario.rol() + " no tiene el permiso " + permiso);
        }
        return usuario;
    }
}
