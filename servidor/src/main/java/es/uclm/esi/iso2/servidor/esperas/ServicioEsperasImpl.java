package es.uclm.esi.iso2.servidor.esperas;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import es.uclm.esi.iso2.servidor.agendas.HuecoLibre;
import es.uclm.esi.iso2.servidor.agendas.ServicioAgendas;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.catalogo.ServicioCatalogo;
import es.uclm.esi.iso2.servidor.citas.Cita;
import es.uclm.esi.iso2.servidor.citas.ServicioCitas;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

public class ServicioEsperasImpl implements ServicioEsperas {

    private static final Comparator<EntradaEspera> ORDEN_DE_ATENCION = Comparator
            .comparing(EntradaEspera::prioridad)
            .thenComparing(EntradaEspera::fechaSolicitud)
            .thenComparingLong(EntradaEspera::idSolicitud);

    private final ServicioSolicitudes solicitudes;
    private final ServicioCatalogo catalogo;
    private final ServicioAgendas agendas;
    private final ServicioCitas citas;
    private final Clock reloj;

    public ServicioEsperasImpl(ServicioSolicitudes solicitudes, ServicioCatalogo catalogo, ServicioAgendas agendas,
            ServicioCitas citas, Clock reloj) {
        this.solicitudes = solicitudes;
        this.catalogo = catalogo;
        this.agendas = agendas;
        this.citas = citas;
        this.reloj = reloj;
    }

    @Override
    public List<EntradaEspera> listaDeEspera() {
        LocalDate hoy = LocalDate.now(reloj);
        return solicitudes.pendientes().stream().map(s -> entrada(s, hoy)).sorted(ORDEN_DE_ATENCION).toList();
    }

    @Override
    public List<EntradaEspera> listaDeEspera(Modalidad modalidad) {
        return listaDeEspera().stream().filter(e -> e.modalidad() == modalidad).toList();
    }

    @Override
    public void cambiarPrioridad(long idSolicitud, Prioridad prioridad) {
        solicitudes.cambiarPrioridad(idSolicitud, prioridad);
    }

    @Override
    public Cita citarEnPrimerHueco(long idSolicitud) {
        SolicitudPrueba solicitud = solicitudes.obtener(idSolicitud);
        if (solicitud.getEstado() != EstadoSolicitud.SOLICITADA) {
            throw new ReglaNegocioException("La solicitud " + idSolicitud + " no está en la lista de espera ("
                    + solicitud.getEstado() + ")");
        }
        EntradaEspera entrada = entrada(solicitud, LocalDate.now(reloj));
        LocalDateTime ahora = LocalDateTime.now(reloj);
        for (HuecoLibre hueco : agendas.huecosLibres(entrada.modalidad(), ahora.toLocalDate(), hastaPara(entrada))) {
            if (hueco.momento().isAfter(ahora)) {
                try {
                    return citas.asignar(idSolicitud, hueco.idAgenda(), hueco.inicio());
                } catch (ReglaNegocioException pacienteOcupado) {
                    // el paciente ya tiene otra cita a esa hora: se prueba el siguiente hueco
                }
            }
        }
        throw new ReglaNegocioException("No hay huecos libres de " + entrada.modalidad() + " hasta el "
                + hastaPara(entrada) + " para la solicitud " + idSolicitud);
    }

    @Override
    public int rellenarHuecos(Modalidad modalidad) {
        int citadas = 0;
        for (EntradaEspera entrada : listaDeEspera(modalidad)) {
            try {
                citarEnPrimerHueco(entrada.idSolicitud());
                citadas++;
            } catch (ReglaNegocioException sinHueco) {
                // queda en la lista de espera; las siguientes pueden tener plazos más amplios
            }
        }
        return citadas;
    }

    private EntradaEspera entrada(SolicitudPrueba s, LocalDate hoy) {
        LocalDate solicitada = s.getFechaSolicitud().toLocalDate();
        LocalDate limite = solicitada.plusDays(plazo(s.getPrioridad()));
        return new EntradaEspera(s.getId(), s.getIdPaciente(), s.getCodigoTipo(),
                catalogo.obtener(s.getCodigoTipo()).modalidad(), s.getPrioridad(), solicitada, limite,
                ChronoUnit.DAYS.between(solicitada, hoy), hoy.isAfter(limite));
    }

    /** Último día en que se busca hueco: la fecha límite, o un nuevo plazo desde hoy si ya ha vencido. */
    private LocalDate hastaPara(EntradaEspera entrada) {
        return entrada.fueraDePlazo() ? LocalDate.now(reloj).plusDays(plazo(entrada.prioridad()))
                : entrada.fechaLimite();
    }

    private int plazo(Prioridad prioridad) {
        return catalogo.parametros().plazoMaximoDias(prioridad);
    }
}
