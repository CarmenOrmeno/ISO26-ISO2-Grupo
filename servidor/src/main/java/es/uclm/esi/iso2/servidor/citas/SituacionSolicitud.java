package es.uclm.esi.iso2.servidor.citas;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

/**
 * Estado de una solicitud y su cita vigente, para que el médico la siga (CU14).
 *
 * @param cita la última cita no cancelada de la solicitud, si la tiene
 */
public record SituacionSolicitud(SolicitudPrueba solicitud, Optional<Cita> cita) {

    /** Elige como cita vigente la más reciente que no está cancelada. */
    public static SituacionSolicitud de(SolicitudPrueba solicitud, List<Cita> citasDeLaSolicitud) {
        return new SituacionSolicitud(solicitud, citasDeLaSolicitud.stream()
                .filter(c -> c.getEstado() != EstadoCita.CANCELADA)
                .max(Comparator.comparingLong(Cita::getId)));
    }

    @Override
    public String toString() {
        return solicitud + cita.map(c -> "\n  " + c).orElse("\n  Sin cita");
    }
}
