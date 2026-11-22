package es.uclm.esi.iso2.servidor.api;

import java.time.LocalDate;

import es.uclm.esi.iso2.servidor.estadisticas.EstadisticasGlobales;

/** CU22 · Generar estadísticas globales y reportes de uso (administrador). */
public interface ApiEstadisticasGlobales {

    /** Estadísticas del periodo entre dos fechas (ambas incluidas) y situación actual. */
    EstadisticasGlobales estadisticas(String token, LocalDate desde, LocalDate hasta);
}
