package es.uclm.esi.iso2.servidor.controlador;

import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiAtencion;
import es.uclm.esi.iso2.servidor.atencion.Encuesta;
import es.uclm.esi.iso2.servidor.atencion.IncidenciaPaciente;
import es.uclm.esi.iso2.servidor.atencion.ServicioAtencion;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;

public class ControladorAtencion implements ApiAtencion {

    private final ServicioAtencion atencion;
    private final ControlAcceso acceso;

    public ControladorAtencion(ServicioAtencion atencion, ControlAcceso acceso) {
        this.atencion = atencion;
        this.acceso = acceso;
    }

    @Override
    public List<IncidenciaPaciente> incidenciasAbiertas(String token) {
        acceso.exigir(token, Permiso.ATENDER_INCIDENCIAS);
        return atencion.incidenciasAbiertas();
    }

    @Override
    public IncidenciaPaciente responderIncidencia(String token, long idIncidencia, String respuesta) {
        String login = acceso.exigir(token, Permiso.ATENDER_INCIDENCIAS).login();
        return atencion.responderIncidencia(idIncidencia, login, respuesta);
    }

    @Override
    public List<Encuesta> encuestas(String token) {
        acceso.exigir(token, Permiso.ATENDER_INCIDENCIAS);
        return atencion.todasLasEncuestas();
    }
}
