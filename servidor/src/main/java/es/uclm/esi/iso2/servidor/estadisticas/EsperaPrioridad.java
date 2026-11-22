package es.uclm.esi.iso2.servidor.estadisticas;

import java.util.Locale;

import es.uclm.esi.iso2.servidor.catalogo.Prioridad;

/**
 * Tiempo de espera hasta la cita de las solicitudes de una prioridad (CU21).
 *
 * @param mediaDias    días medios entre la solicitud y el día de su cita
 * @param fueraDePlazo citas cuya espera supera el plazo máximo de la prioridad (CU03)
 */
public record EsperaPrioridad(Prioridad prioridad, long citas, double mediaDias, int plazoMaximoDias,
        long fueraDePlazo) {

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s: %d citas, %.1f días de media, %d fuera de plazo (máx. %d días)",
                prioridad, citas, mediaDias, fueraDePlazo, plazoMaximoDias);
    }
}
