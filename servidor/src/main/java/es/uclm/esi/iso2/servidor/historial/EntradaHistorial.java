package es.uclm.esi.iso2.servidor.historial;

import java.time.LocalDateTime;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;

/**
 * Prueba realizada del historial del paciente (CU13).
 *
 * @param informeDisponible la prueba está INFORMADA y su informe se puede consultar
 */
public record EntradaHistorial(long idSolicitud, String codigoTipo, String nombreTipo, Modalidad modalidad,
        LocalDateTime fechaRealizacion, EstadoSolicitud estado, boolean informeDisponible) {

    @Override
    public String toString() {
        return fechaRealizacion.toLocalDate() + " · " + nombreTipo + " (" + modalidad + ") · solicitud " + idSolicitud
                + (informeDisponible ? " · informe disponible" : " · pendiente de informe");
    }
}
