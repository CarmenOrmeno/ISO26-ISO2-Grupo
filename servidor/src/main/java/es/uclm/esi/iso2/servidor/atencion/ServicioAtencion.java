package es.uclm.esi.iso2.servidor.atencion;

import java.util.List;

import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

/** Contrato interno de la atención al paciente: incidencias y encuestas de satisfacción (CU15). */
public interface ServicioAtencion {

    /**
     * El paciente comunica una incidencia. Si indica una prueba, debe ser suya.
     *
     * @param idSolicitud prueba a la que se refiere, o {@code null} si es general
     */
    IncidenciaPaciente comunicarIncidencia(String idPaciente, CategoriaIncidencia categoria, String descripcion,
            Long idSolicitud);

    /** Incidencias del paciente, de la más reciente a la más antigua. */
    List<IncidenciaPaciente> incidenciasDe(String idPaciente);

    /** Incidencias sin responder, de la más antigua a la más reciente (orden de atención). */
    List<IncidenciaPaciente> incidenciasAbiertas();

    IncidenciaPaciente responderIncidencia(long idIncidencia, String login, String respuesta);

    /** Pruebas realizadas del paciente que aún no tienen encuesta. */
    List<SolicitudPrueba> encuestasPendientes(String idPaciente);

    /** El paciente valora una prueba suya ya realizada; una sola vez por prueba. */
    Encuesta responderEncuesta(String idPaciente, long idSolicitud, int puntuacion, String comentario);

    List<Encuesta> encuestasDe(String idPaciente);

    List<Encuesta> todasLasEncuestas();
}
