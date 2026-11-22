package es.uclm.esi.iso2.servidor.estadisticas;

/** Contrato interno de las estadísticas del médico (CU20). */
public interface ServicioEstadisticasMedico {

    EstadisticasMedico estadisticas(String loginMedico, Periodo periodo);
}
