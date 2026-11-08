package es.uclm.esi.iso2.servidor.citas;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import es.uclm.esi.iso2.servidor.agendas.Agenda;
import es.uclm.esi.iso2.servidor.agendas.ServicioAgendas;
import es.uclm.esi.iso2.servidor.catalogo.ServicioCatalogo;
import es.uclm.esi.iso2.servidor.catalogo.TipoPrueba;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.notificaciones.Notificador;
import es.uclm.esi.iso2.servidor.notificaciones.TipoNotificacion;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

public class ServicioCitasImpl implements ServicioCitas {

    private final Repositorio<Cita, Long> repositorio;
    private final ServicioSolicitudes solicitudes;
    private final ServicioAgendas agendas;
    private final ServicioCatalogo catalogo;
    private final Notificador notificador;
    private final Clock reloj;
    private long secuencia;

    public ServicioCitasImpl(Repositorio<Cita, Long> repositorio, ServicioSolicitudes solicitudes,
            ServicioAgendas agendas, ServicioCatalogo catalogo, Notificador notificador, Clock reloj) {
        this.repositorio = repositorio;
        this.solicitudes = solicitudes;
        this.agendas = agendas;
        this.catalogo = catalogo;
        this.notificador = notificador;
        this.reloj = reloj;
    }

    @Override
    public Cita asignar(long idSolicitud, long idAgenda, LocalTime hora) {
        SolicitudPrueba solicitud = solicitudes.obtener(idSolicitud);
        if (solicitud.getEstado() != EstadoSolicitud.SOLICITADA) {
            throw new ReglaNegocioException("La solicitud " + idSolicitud + " no está pendiente de cita ("
                    + solicitud.getEstado() + ")");
        }
        LocalDateTime momento = comprobarHueco(solicitud, idAgenda, hora, null);
        Cita cita = new Cita(secuencia + 1, idSolicitud, solicitud.getIdPaciente(), idAgenda, momento);
        agendas.ocupar(idAgenda, hora, cita.getId());
        solicitudes.marcarCitada(idSolicitud);
        secuencia++;
        repositorio.guardar(cita);
        avisar(cita, TipoNotificacion.CITA_ASIGNADA, "Cita asignada");
        return cita;
    }

    @Override
    public Cita reprogramar(long idCita, long idAgenda, LocalTime hora) {
        Cita cita = repositorio.obtener(idCita);
        if (!cita.getEstado().activa()) {
            throw new ReglaNegocioException("No se puede reprogramar la cita " + idCita + " en estado " + cita.getEstado());
        }
        SolicitudPrueba solicitud = solicitudes.obtener(cita.getIdSolicitud());
        LocalDateTime momento = comprobarHueco(solicitud, idAgenda, hora, cita.getId());
        agendas.ocupar(idAgenda, hora, cita.getId());
        agendas.liberar(cita.getIdAgenda(), cita.getFechaHora().toLocalTime());
        cita.reprogramar(idAgenda, momento);
        repositorio.guardar(cita);
        avisar(cita, TipoNotificacion.CITA_MODIFICADA, "Cita modificada");
        return cita;
    }

    @Override
    public void cancelar(long idCita, String motivo) {
        Cita cita = repositorio.obtener(idCita);
        cita.cancelar(motivo, LocalDateTime.now(reloj));
        agendas.liberar(cita.getIdAgenda(), cita.getFechaHora().toLocalTime());
        solicitudes.anularCita(cita.getIdSolicitud());
        repositorio.guardar(cita);
        notificador.notificar(cita.getIdPaciente(), TipoNotificacion.CITA_CANCELADA,
                "Su cita del " + cita.getFechaHora() + " se ha cancelado: " + motivo);
    }

    @Override
    public Cita confirmar(long idCita, String idPaciente) {
        Cita cita = citaFuturaDe(idCita, idPaciente);
        cita.confirmar();
        return repositorio.guardar(cita);
    }

    @Override
    public Cita solicitarCambio(long idCita, String idPaciente, String motivo) {
        Cita cita = citaFuturaDe(idCita, idPaciente);
        cita.solicitarCambio(motivo);
        return repositorio.guardar(cita);
    }

    @Override
    public void cancelarPorPaciente(long idCita, String idPaciente, String motivo) {
        citaFuturaDe(idCita, idPaciente);
        cancelar(idCita, motivo);
    }

    @Override
    public List<Cita> conCambioSolicitado() {
        return repositorio.filtrar(c -> c.getEstado() == EstadoCita.CAMBIO_SOLICITADO);
    }

    /** La cita debe ser del paciente y no haber pasado. */
    private Cita citaFuturaDe(long idCita, String idPaciente) {
        Cita cita = repositorio.obtener(idCita);
        if (!cita.getIdPaciente().equals(idPaciente)) {
            throw new AccesoDenegadoException("La cita " + idCita + " no es suya");
        }
        if (!cita.getFechaHora().isAfter(LocalDateTime.now(reloj))) {
            throw new ReglaNegocioException("La cita " + idCita + " ya ha pasado");
        }
        return cita;
    }

    private void avisar(Cita cita, TipoNotificacion tipo, String titulo) {
        Agenda agenda = agendas.obtener(cita.getIdAgenda());
        TipoPrueba prueba = catalogo.obtener(solicitudes.obtener(cita.getIdSolicitud()).getCodigoTipo());
        notificador.notificar(cita.getIdPaciente(), tipo, titulo + ": " + prueba.nombre() + " el "
                + cita.getFechaHora().toLocalDate() + " a las " + cita.getFechaHora().toLocalTime()
                + " (sala " + agenda.getSala() + ")");
    }

    /**
     * Comprueba que el hueco es compatible con la solicitud: misma modalidad que el tipo de prueba,
     * en el futuro, libre, y que el paciente no tiene otra cita activa a la misma hora.
     */
    private LocalDateTime comprobarHueco(SolicitudPrueba solicitud, long idAgenda, LocalTime hora, Long citaActual) {
        Agenda agenda = agendas.obtener(idAgenda);
        TipoPrueba tipo = catalogo.obtener(solicitud.getCodigoTipo());
        if (agenda.getModalidad() != tipo.modalidad()) {
            throw new ReglaNegocioException("La agenda es de " + agenda.getModalidad() + " y la prueba de "
                    + tipo.modalidad());
        }
        if (!agenda.hueco(hora).libre()) {
            throw new ReglaNegocioException("El hueco de las " + hora + " ya está ocupado");
        }
        LocalDateTime momento = agenda.getFecha().atTime(hora);
        if (!momento.isAfter(LocalDateTime.now(reloj))) {
            throw new ReglaNegocioException("No se puede citar en el pasado: " + momento);
        }
        boolean coincide = repositorio.filtrar(c -> c.getIdPaciente().equals(solicitud.getIdPaciente())
                && c.getEstado().activa() && c.getFechaHora().equals(momento)
                && (citaActual == null || c.getId() != citaActual)).size() > 0;
        if (coincide) {
            throw new ReglaNegocioException("El paciente ya tiene otra cita a las " + momento);
        }
        return momento;
    }

    @Override
    public Cita obtener(long idCita) {
        return repositorio.obtener(idCita);
    }

    @Override
    public List<Cita> dePaciente(String idPaciente) {
        return repositorio.filtrar(c -> c.getIdPaciente().equals(idPaciente)).stream()
                .sorted(Comparator.comparing(Cita::getFechaHora)).toList();
    }

    @Override
    public List<Cita> delDia(LocalDate fecha) {
        return repositorio.filtrar(c -> c.getFechaHora().toLocalDate().equals(fecha)).stream()
                .sorted(Comparator.comparing(Cita::getFechaHora)).toList();
    }

    @Override
    public List<Cita> todas() {
        return repositorio.todos();
    }

    @Override
    public Cita marcarRealizada(long idCita) {
        Cita cita = repositorio.obtener(idCita);
        cita.marcarRealizada();
        return repositorio.guardar(cita);
    }
}
