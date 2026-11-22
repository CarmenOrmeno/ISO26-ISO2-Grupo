package es.uclm.esi.iso2.servidor.estadisticas;

import java.util.Map;
import java.util.OptionalDouble;

import es.uclm.esi.iso2.servidor.seguridad.Rol;

/**
 * Estadísticas globales y de uso del sistema para el administrador (CU22).
 *
 * @param pacientesActivos     pacientes dados de alta (en este momento)
 * @param usuariosPorRol       usuarios de la aplicación por rol (en este momento; todos los roles)
 * @param sesionesIniciadas    sesiones iniciadas desde el arranque del servidor (las sesiones no se guardan)
 * @param solicitudes          solicitudes hechas en el periodo
 * @param citas                citas con fecha en el periodo (también las canceladas)
 * @param pruebasRealizadas    pruebas realizadas en el periodo
 * @param informesEmitidos     informes radiológicos emitidos en el periodo
 * @param encuestas            encuestas de satisfacción respondidas en el periodo (CU15)
 * @param satisfaccionMedia    puntuación media de esas encuestas (1..5), vacía si no hay
 * @param incidenciasAbiertas  incidencias de pacientes sin responder (en este momento)
 * @param accesosAuditados     registros de auditoría del periodo (CU18)
 * @param accesosDenegados     de ellos, los denegados
 */
public record EstadisticasGlobales(Periodo periodo, long pacientesActivos, Map<Rol, Long> usuariosPorRol,
        long sesionesIniciadas, long solicitudes, long citas, long pruebasRealizadas, long informesEmitidos,
        long encuestas, OptionalDouble satisfaccionMedia, long incidenciasAbiertas, long accesosAuditados,
        long accesosDenegados) {

    public EstadisticasGlobales {
        usuariosPorRol = Map.copyOf(usuariosPorRol);
    }

    @Override
    public String toString() {
        return "Estadísticas globales · " + periodo
                + "\n  Pacientes activos: " + pacientesActivos + " · usuarios: " + usuariosPorRol
                + " · sesiones desde el arranque: " + sesionesIniciadas
                + "\n  Solicitudes: " + solicitudes + " · citas: " + citas + " · realizadas: " + pruebasRealizadas
                + " · informes: " + informesEmitidos
                + "\n  Encuestas: " + encuestas + " · satisfacción media: " + Porcentaje.texto(satisfaccionMedia, "/5")
                + " · incidencias abiertas: " + incidenciasAbiertas
                + "\n  Accesos auditados: " + accesosAuditados + " (" + accesosDenegados + " denegados)";
    }
}
