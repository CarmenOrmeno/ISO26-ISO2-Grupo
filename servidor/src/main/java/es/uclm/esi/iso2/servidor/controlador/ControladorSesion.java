package es.uclm.esi.iso2.servidor.controlador;

import es.uclm.esi.iso2.servidor.api.ApiSesion;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.seguridad.ServicioSesion;
import es.uclm.esi.iso2.servidor.seguridad.Usuario;

public class ControladorSesion implements ApiSesion {

    private final ServicioSesion sesiones;
    private final ControlAcceso acceso;

    public ControladorSesion(ServicioSesion sesiones, ControlAcceso acceso) {
        this.sesiones = sesiones;
        this.acceso = acceso;
    }

    @Override
    public String iniciarSesion(String login, String contrasena) {
        return sesiones.iniciarSesion(login, contrasena).getToken();
    }

    @Override
    public void cerrarSesion(String token) {
        sesiones.cerrarSesion(token);
    }

    @Override
    public Usuario usuarioActual(String token) {
        return acceso.exigir(token, Permiso.CONSULTAR_PERFIL);
    }
}
