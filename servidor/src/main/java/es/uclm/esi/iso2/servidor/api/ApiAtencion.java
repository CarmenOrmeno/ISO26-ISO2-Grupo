package es.uclm.esi.iso2.servidor.api;

import java.util.List;

import es.uclm.esi.iso2.servidor.atencion.Encuesta;
import es.uclm.esi.iso2.servidor.atencion.IncidenciaPaciente;

/** CU15 · Atender las incidencias de los pacientes y consultar las encuestas (personal de admisión). */
public interface ApiAtencion {

    List<IncidenciaPaciente> incidenciasAbiertas(String token);

    IncidenciaPaciente responderIncidencia(String token, long idIncidencia, String respuesta);

    List<Encuesta> encuestas(String token);
}
