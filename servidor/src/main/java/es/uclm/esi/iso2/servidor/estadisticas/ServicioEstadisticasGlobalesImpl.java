package es.uclm.esi.iso2.servidor.estadisticas;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import es.uclm.esi.iso2.servidor.atencion.Encuesta;
import es.uclm.esi.iso2.servidor.atencion.ServicioAtencion;
import es.uclm.esi.iso2.servidor.auditoria.ServicioAuditoria;
import es.uclm.esi.iso2.servidor.citas.ServicioCitas;
import es.uclm.esi.iso2.servidor.informes.ServicioInformes;
import es.uclm.esi.iso2.servidor.pacientes.ServicioPacientes;
import es.uclm.esi.iso2.servidor.seguridad.Rol;
import es.uclm.esi.iso2.servidor.seguridad.ServicioPermisos;
import es.uclm.esi.iso2.servidor.seguridad.ServicioSesion;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;

/** Solo lee de los demás componentes a través de sus interfaces; no tiene almacén propio. */
public class ServicioEstadisticasGlobalesImpl implements ServicioEstadisticasGlobales {

    /** Componentes de los que se leen los datos (agrupados para no tener un constructor de doce parámetros). */
    public record Fuentes(ServicioPacientes pacientes, ServicioPermisos usuarios, ServicioSesion sesiones,
            ServicioSolicitudes solicitudes, ServicioCitas citas, ServicioInformes informes,
            ServicioAtencion atencion, ServicioAuditoria auditoria) {
    }

    private final Fuentes f;

    public ServicioEstadisticasGlobalesImpl(Fuentes fuentes) {
        this.f = Objects.requireNonNull(fuentes);
    }

    @Override
    public EstadisticasGlobales estadisticas(Periodo periodo) {
        Objects.requireNonNull(periodo, "periodo");
        Map<Rol, Long> porRol = new EnumMap<>(Rol.class);
        Arrays.stream(Rol.values()).forEach(r -> porRol.put(r, 0L));
        f.usuarios().usuarios().forEach(u -> porRol.merge(u.rol(), 1L, Long::sum));

        var encuestas = f.atencion().todasLasEncuestas().stream().filter(e -> periodo.contiene(e.fecha())).toList();

        return new EstadisticasGlobales(periodo,
                f.pacientes().activos().size(),
                porRol,
                f.sesiones().sesionesIniciadas(),
                f.solicitudes().todas().stream().filter(s -> periodo.contiene(s.getFechaSolicitud())).count(),
                f.citas().todas().stream().filter(c -> periodo.contiene(c.getFechaHora())).count(),
                f.solicitudes().todas().stream().filter(s -> periodo.contiene(s.getFechaRealizacion())).count(),
                f.informes().todos().stream().filter(i -> periodo.contiene(i.fecha())).count(),
                encuestas.size(),
                encuestas.stream().mapToInt(Encuesta::puntuacion).average(),
                f.atencion().incidenciasAbiertas().size(),
                f.auditoria().registros(periodo.desde(), periodo.hasta()).size(),
                f.auditoria().denegados(periodo.desde(), periodo.hasta()).size());
    }
}
