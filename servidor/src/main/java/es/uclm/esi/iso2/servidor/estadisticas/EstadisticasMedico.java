package es.uclm.esi.iso2.servidor.estadisticas;

import java.util.Map;
import java.util.OptionalDouble;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;

/**
 * Estadísticas de las solicitudes de un médico en un periodo (CU20). Solo cuentan las solicitudes hechas en el
 * periodo (fecha de solicitud).
 *
 * @param porEstado                 solicitudes por estado (todos los estados, con 0 si no hay)
 * @param porModalidad              uso de recursos: solicitudes por modalidad (solo las que tienen alguna)
 * @param esperaMediaDias           días medios entre la solicitud y su realización, por prioridad (solo las
 *                                  prioridades con alguna prueba realizada)
 * @param revisionesGeneradas       revisiones de sus seguimientos generadas en el periodo
 * @param revisionesRealizadas      de ellas, las ya realizadas o informadas
 * @param adherenciaSeguimientos    revisionesRealizadas / revisionesGeneradas en %, vacío si no hay revisiones
 */
public record EstadisticasMedico(String loginMedico, Periodo periodo, long totalSolicitudes,
        Map<EstadoSolicitud, Long> porEstado, Map<Modalidad, Long> porModalidad,
        Map<Prioridad, Double> esperaMediaDias, long revisionesGeneradas, long revisionesRealizadas,
        OptionalDouble adherenciaSeguimientos) {

    public EstadisticasMedico {
        porEstado = Map.copyOf(porEstado);
        porModalidad = Map.copyOf(porModalidad);
        esperaMediaDias = Map.copyOf(esperaMediaDias);
    }

    @Override
    public String toString() {
        return "Estadísticas de " + loginMedico + " · " + periodo + "\n  Solicitudes: " + totalSolicitudes + " "
                + porEstado + "\n  Por modalidad: " + porModalidad + "\n  Espera media (días): " + esperaMediaDias
                + "\n  Adherencia a seguimientos: " + Porcentaje.texto(adherenciaSeguimientos, " %") + " ("
                + revisionesRealizadas + " de " + revisionesGeneradas + " revisiones)";
    }
}
