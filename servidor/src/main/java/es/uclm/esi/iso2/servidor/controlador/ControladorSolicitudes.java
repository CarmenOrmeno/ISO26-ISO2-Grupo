package es.uclm.esi.iso2.servidor.controlador;

import es.uclm.esi.iso2.servidor.api.ApiSolicitudes;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.seguridad.Rol;
import es.uclm.esi.iso2.servidor.seguridad.Usuario;
import es.uclm.esi.iso2.servidor.solicitudes.DatosSolicitud;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

public class ControladorSolicitudes implements ApiSolicitudes {

    private final ServicioSolicitudes servicio;
    private final ControlAcceso acceso;

    public ControladorSolicitudes(ServicioSolicitudes servicio, ControlAcceso acceso) {
        this.servicio = servicio;
        this.acceso = acceso;
    }

    @Override
    public SolicitudPrueba solicitar(String token, DatosSolicitud datos) {
        Usuario medico = acceso.exigir(token, Permiso.SOLICITAR_PRUEBAS);
        return servicio.solicitar(medico.login(), datos);
    }

    @Override
    public void cancelar(String token, long idSolicitud, String motivo) {
        Usuario medico = acceso.exigir(token, Permiso.SOLICITAR_PRUEBAS);
        servicio.cancelar(idSolicitud, medico.login(), motivo);
    }

    @Override
    public SolicitudPrueba obtener(String token, long idSolicitud) {
        Usuario usuario = acceso.exigir(token, Permiso.CONSULTAR_SOLICITUDES);
        SolicitudPrueba s = servicio.obtener(idSolicitud);
        if (usuario.rol() == Rol.MEDICO && !s.getLoginMedico().equals(usuario.login())) {
            throw new AccesoDenegadoException("La solicitud " + idSolicitud + " no es suya");
        }
        return s;
    }
}
