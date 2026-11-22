package es.uclm.esi.iso2.servidor.api;

import java.util.List;

import es.uclm.esi.iso2.servidor.citas.Cita;
import es.uclm.esi.iso2.servidor.realizacion.Incidencia;
import es.uclm.esi.iso2.servidor.realizacion.TipoIncidencia;

/** CU08 · Registrar realización de la prueba e incidencias (médico de la agenda). */
public interface ApiRealizacion {

    Cita registrarRealizacion(String token, long idCita, int numeroImagenes, String observaciones);

    Incidencia registrarIncidencia(String token, long idCita, TipoIncidencia tipo, String descripcion);

    List<Incidencia> incidenciasDeCita(String token, long idCita);
}
