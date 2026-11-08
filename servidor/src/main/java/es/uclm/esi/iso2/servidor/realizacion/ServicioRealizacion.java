package es.uclm.esi.iso2.servidor.realizacion;

import java.util.List;

import es.uclm.esi.iso2.servidor.citas.Cita;

/** Contrato interno del registro de realización de pruebas (CU08). */
public interface ServicioRealizacion {

    /**
     * Registra que la prueba de la cita se ha realizado: cita REALIZADA, solicitud REALIZADA y estudio
     * registrado en el PACS con el número de imágenes adquiridas.
     */
    Cita registrarRealizacion(long idCita, String loginMedico, int numeroImagenes, String observaciones);

    Incidencia registrarIncidencia(long idCita, String loginMedico, TipoIncidencia tipo, String descripcion);

    List<Incidencia> incidenciasDeCita(long idCita);

    List<Incidencia> todasLasIncidencias();
}
