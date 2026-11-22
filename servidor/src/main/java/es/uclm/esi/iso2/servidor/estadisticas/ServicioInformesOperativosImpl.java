package es.uclm.esi.iso2.servidor.estadisticas;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import es.uclm.esi.iso2.servidor.agendas.Agenda;
import es.uclm.esi.iso2.servidor.agendas.ServicioAgendas;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.catalogo.ServicioCatalogo;
import es.uclm.esi.iso2.servidor.citas.Cita;
import es.uclm.esi.iso2.servidor.citas.EstadoCita;
import es.uclm.esi.iso2.servidor.citas.ServicioCitas;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

public class ServicioInformesOperativosImpl implements ServicioInformesOperativos {

    private final ServicioAgendas agendas;
    private final ServicioCitas citas;
    private final ServicioSolicitudes solicitudes;
    private final ServicioCatalogo catalogo;

    public ServicioInformesOperativosImpl(ServicioAgendas agendas, ServicioCitas citas,
            ServicioSolicitudes solicitudes, ServicioCatalogo catalogo) {
        this.agendas = agendas;
        this.citas = citas;
        this.solicitudes = solicitudes;
        this.catalogo = catalogo;
    }

    @Override
    public InformeOperativo generar(Periodo periodo) {
        Objects.requireNonNull(periodo, "periodo");
        List<Cita> delPeriodo = citas.todas().stream().filter(c -> periodo.contiene(c.getFechaHora())).toList();
        long canceladas = delPeriodo.stream().filter(c -> c.getEstado() == EstadoCita.CANCELADA).count();
        return new InformeOperativo(periodo, ocupacion(periodo), esperas(delPeriodo), delPeriodo.size(), canceladas);
    }

    private List<OcupacionModalidad> ocupacion(Periodo periodo) {
        Map<Modalidad, long[]> cuentas = new EnumMap<>(Modalidad.class);
        for (Agenda agenda : agendas.todas()) {
            if (periodo.contiene(agenda.getFecha())) {
                long[] c = cuentas.computeIfAbsent(agenda.getModalidad(), m -> new long[2]);
                c[0] += agenda.getHuecos().size();
                c[1] += agenda.huecosOcupados();
            }
        }
        List<OcupacionModalidad> resultado = new ArrayList<>();
        cuentas.forEach((m, c) -> resultado.add(new OcupacionModalidad(m, c[0], c[1])));
        return resultado;
    }

    private List<EsperaPrioridad> esperas(List<Cita> delPeriodo) {
        Map<Prioridad, List<Long>> dias = new EnumMap<>(Prioridad.class);
        for (Cita cita : delPeriodo) {
            if (cita.getEstado() != EstadoCita.CANCELADA) {
                SolicitudPrueba s = solicitudes.obtener(cita.getIdSolicitud());
                dias.computeIfAbsent(s.getPrioridad(), p -> new ArrayList<>()).add(ChronoUnit.DAYS
                        .between(s.getFechaSolicitud().toLocalDate(), cita.getFechaHora().toLocalDate()));
            }
        }
        List<EsperaPrioridad> resultado = new ArrayList<>();
        dias.forEach((prioridad, lista) -> {
            int plazo = catalogo.parametros().plazoMaximoDias(prioridad);
            resultado.add(new EsperaPrioridad(prioridad, lista.size(),
                    lista.stream().mapToLong(Long::longValue).average().orElse(0),
                    plazo, lista.stream().filter(d -> d > plazo).count()));
        });
        return resultado;
    }
}
