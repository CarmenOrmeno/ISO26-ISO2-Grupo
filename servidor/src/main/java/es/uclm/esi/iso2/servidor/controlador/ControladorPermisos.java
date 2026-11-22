package es.uclm.esi.iso2.servidor.controlador;

import java.util.List;
import java.util.Set;

import es.uclm.esi.iso2.servidor.api.ApiPermisos;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.seguridad.Rol;
import es.uclm.esi.iso2.servidor.seguridad.ServicioPermisos;
import es.uclm.esi.iso2.servidor.seguridad.Usuario;

public class ControladorPermisos implements ApiPermisos {

    private final ServicioPermisos permisos;
    private final ControlAcceso acceso;

    public ControladorPermisos(ServicioPermisos permisos, ControlAcceso acceso) {
        this.permisos = permisos;
        this.acceso = acceso;
    }

    private String administrador(String token) {
        return acceso.exigir(token, Permiso.GESTIONAR_PERMISOS).login();
    }

    @Override
    public Set<Permiso> permisosDe(String token, Rol rol) {
        administrador(token);
        return permisos.permisosDe(rol);
    }

    @Override
    public void conceder(String token, Rol rol, Permiso permiso) {
        administrador(token);
        permisos.conceder(rol, permiso);
    }

    @Override
    public void revocar(String token, Rol rol, Permiso permiso) {
        administrador(token);
        permisos.revocar(rol, permiso);
    }

    @Override
    public void restablecer(String token) {
        administrador(token);
        permisos.restablecer();
    }

    @Override
    public List<Usuario> usuarios(String token) {
        administrador(token);
        return permisos.usuarios();
    }

    @Override
    public Usuario cambiarRol(String token, String login, Rol nuevoRol) {
        return permisos.cambiarRol(administrador(token), login, nuevoRol);
    }
}
