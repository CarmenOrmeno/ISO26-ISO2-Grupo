package es.uclm.esi.iso2.servidor.api;

import java.time.LocalDate;

import es.uclm.esi.iso2.servidor.estadisticas.EstadisticasMedico;

/** CU20 · Consultar estadísticas de sus solicitudes: espera, recursos y adherencia (médico). */
public interface ApiEstadisticasMedico {

    /** Estadísticas de las solicitudes que hizo entre dos fechas (ambas incluidas). */
    EstadisticasMedico misEstadisticas(String token, LocalDate desde, LocalDate hasta);
}
