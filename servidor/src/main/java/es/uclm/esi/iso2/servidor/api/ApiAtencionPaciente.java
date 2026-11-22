package es.uclm.esi.iso2.servidor.api;

import java.util.List;

import es.uclm.esi.iso2.servidor.atencion.CategoriaIncidencia;
import es.uclm.esi.iso2.servidor.atencion.Encuesta;
import es.uclm.esi.iso2.servidor.atencion.IncidenciaPaciente;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

/** CU15 · Comunicar incidencias y responder encuestas de satisfacción (paciente; el médico lo hereda). */
public interface ApiAtencionPaciente {

    /** @param idSolicitud prueba suya a la que se refiere, o {@code null} si es una incidencia general */
    IncidenciaPaciente comunicarIncidencia(String token, CategoriaIncidencia categoria, String descripcion,
            Long idSolicitud);

    List<IncidenciaPaciente> misIncidencias(String token);

    /** Sus pruebas realizadas que aún no ha valorado. */
    List<SolicitudPrueba> encuestasPendientes(String token);

    Encuesta responderEncuesta(String token, long idSolicitud, int puntuacion, String comentario);

    List<Encuesta> misEncuestas(String token);
}
