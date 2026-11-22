package es.uclm.esi.iso2.servidor.estadisticas;

/** Contrato interno de las estadísticas globales y de uso (CU22). */
public interface ServicioEstadisticasGlobales {

    EstadisticasGlobales estadisticas(Periodo periodo);
}
