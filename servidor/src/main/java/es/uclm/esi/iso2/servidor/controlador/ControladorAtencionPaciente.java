package es.uclm.esi.iso2.servidor.controlador;

import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiAtencionPaciente;
import es.uclm.esi.iso2.servidor.atencion.CategoriaIncidencia;
import es.uclm.esi.iso2.servidor.atencion.Encuesta;
import es.uclm.esi.iso2.servidor.atencion.IncidenciaPaciente;
import es.uclm.esi.iso2.servidor.atencion.ServicioAtencion;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

/** El paciente solo comunica y consulta lo suyo (RNF-S3). */
public class ControladorAtencionPaciente implements ApiAtencionPaciente {

    private final ServicioAtencion atencion;
    private final ControlAcceso acceso;

    public ControladorAtencionPaciente(ServicioAtencion atencion, ControlAcceso acceso) {
        this.atencion = atencion;
        this.acceso = acceso;
    }

    private String paciente(String token, Permiso permiso) {
        return acceso.exigir(token, permiso).exigirPaciente();
    }

    @Override
    public IncidenciaPaciente comunicarIncidencia(String token, CategoriaIncidencia categoria, String descripcion,
            Long idSolicitud) {
        return atencion.comunicarIncidencia(paciente(token, Permiso.COMUNICAR_INCIDENCIAS), categoria, descripcion,
                idSolicitud);
    }

    @Override
    public List<IncidenciaPaciente> misIncidencias(String token) {
        return atencion.incidenciasDe(paciente(token, Permiso.COMUNICAR_INCIDENCIAS));
    }

    @Override
    public List<SolicitudPrueba> encuestasPendientes(String token) {
        return atencion.encuestasPendientes(paciente(token, Permiso.RESPONDER_ENCUESTAS));
    }

    @Override
    public Encuesta responderEncuesta(String token, long idSolicitud, int puntuacion, String comentario) {
        return atencion.responderEncuesta(paciente(token, Permiso.RESPONDER_ENCUESTAS), idSolicitud, puntuacion,
                comentario);
    }

    @Override
    public List<Encuesta> misEncuestas(String token) {
        return atencion.encuestasDe(paciente(token, Permiso.RESPONDER_ENCUESTAS));
    }
}
