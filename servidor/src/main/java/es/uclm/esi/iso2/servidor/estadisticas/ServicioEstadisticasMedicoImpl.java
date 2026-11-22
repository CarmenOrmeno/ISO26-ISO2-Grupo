package es.uclm.esi.iso2.servidor.estadisticas;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.catalogo.ServicioCatalogo;
import es.uclm.esi.iso2.servidor.seguimiento.ServicioSeguimiento;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

public class ServicioEstadisticasMedicoImpl implements ServicioEstadisticasMedico {

    private final ServicioSolicitudes solicitudes;
    private final ServicioCatalogo catalogo;
    private final ServicioSeguimiento seguimientos;

    public ServicioEstadisticasMedicoImpl(ServicioSolicitudes solicitudes, ServicioCatalogo catalogo,
            ServicioSeguimiento seguimientos) {
        this.solicitudes = solicitudes;
        this.catalogo = catalogo;
        this.seguimientos = seguimientos;
    }

    @Override
    public EstadisticasMedico estadisticas(String loginMedico, Periodo periodo) {
        Objects.requireNonNull(periodo, "periodo");
        List<SolicitudPrueba> suyas = solicitudes.porMedico(loginMedico).stream()
                .filter(s -> periodo.contiene(s.getFechaSolicitud()))
                .toList();

        Map<EstadoSolicitud, Long> porEstado = new EnumMap<>(EstadoSolicitud.class);
        Arrays.stream(EstadoSolicitud.values()).forEach(e -> porEstado.put(e, 0L));
        suyas.forEach(s -> porEstado.merge(s.getEstado(), 1L, Long::sum));

        Map<Modalidad, Long> porModalidad = suyas.stream().collect(Collectors.groupingBy(
                s -> catalogo.obtener(s.getCodigoTipo()).modalidad(), () -> new EnumMap<>(Modalidad.class),
                Collectors.counting()));

        Map<Prioridad, Double> espera = suyas.stream().filter(s -> s.getFechaRealizacion() != null)
                .collect(Collectors.groupingBy(SolicitudPrueba::getPrioridad, () -> new EnumMap<>(Prioridad.class),
                        Collectors.averagingLong(s -> ChronoUnit.DAYS.between(s.getFechaSolicitud().toLocalDate(),
                                s.getFechaRealizacion().toLocalDate()))));

        List<SolicitudPrueba> revisiones = suyas.stream().filter(s -> s.getIdSeguimiento() != null)
                .filter(s -> seguimientos.obtener(s.getIdSeguimiento()).getLoginMedico().equals(loginMedico))
                .toList();
        long realizadas = revisiones.stream().filter(s -> s.getFechaRealizacion() != null).count();

        return new EstadisticasMedico(loginMedico, periodo, suyas.size(), porEstado, porModalidad, espera,
                revisiones.size(), realizadas, Porcentaje.de(realizadas, revisiones.size()));
    }
}
