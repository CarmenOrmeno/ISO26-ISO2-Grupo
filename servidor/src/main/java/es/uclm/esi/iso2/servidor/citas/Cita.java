package es.uclm.esi.iso2.servidor.citas;

import java.time.LocalDateTime;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Validaciones;

/** Cita asignada a una solicitud en un hueco de agenda (CU06). */
public class Cita {

    private final long id;
    private final long idSolicitud;
    private final String idPaciente;
    private long idAgenda;
    private LocalDateTime fechaHora;
    private EstadoCita estado = EstadoCita.PROGRAMADA;
    private int reprogramaciones;
    private String motivoCancelacion = "";
    private LocalDateTime fechaCancelacion;
    private String motivoCambio = "";

    public Cita(long id, long idSolicitud, String idPaciente, long idAgenda, LocalDateTime fechaHora) {
        this.id = id;
        this.idSolicitud = idSolicitud;
        this.idPaciente = Validaciones.texto(idPaciente, "paciente");
        this.idAgenda = idAgenda;
        this.fechaHora = Validaciones.obligatorio(fechaHora, "fecha y hora");
    }

    private void exigirActiva(String operacion) {
        if (!estado.activa()) {
            throw new ReglaNegocioException("No se puede " + operacion + " la cita " + id + " en estado " + estado);
        }
    }

    /** Mueve la cita a otro hueco; vuelve a PROGRAMADA (hay que confirmarla de nuevo). */
    public void reprogramar(long nuevaAgenda, LocalDateTime nuevaFechaHora) {
        exigirActiva("reprogramar");
        idAgenda = nuevaAgenda;
        fechaHora = Validaciones.obligatorio(nuevaFechaHora, "fecha y hora");
        estado = EstadoCita.PROGRAMADA;
        reprogramaciones++;
    }

    /** CU07: el paciente confirma su asistencia (solo desde PROGRAMADA). */
    public void confirmar() {
        if (estado != EstadoCita.PROGRAMADA) {
            throw new ReglaNegocioException("Solo se puede confirmar una cita programada (está " + estado + ")");
        }
        estado = EstadoCita.CONFIRMADA;
    }

    /** CU07: el paciente pide un cambio; admisión la reprogramará (CU06). */
    public void solicitarCambio(String motivo) {
        if (estado != EstadoCita.PROGRAMADA && estado != EstadoCita.CONFIRMADA) {
            throw new ReglaNegocioException("No se puede pedir un cambio de una cita " + estado);
        }
        motivoCambio = Validaciones.texto(motivo, "motivo del cambio");
        estado = EstadoCita.CAMBIO_SOLICITADO;
    }

    public void cancelar(String motivo, LocalDateTime cuando) {
        exigirActiva("cancelar");
        motivoCancelacion = Validaciones.texto(motivo, "motivo");
        fechaCancelacion = cuando;
        estado = EstadoCita.CANCELADA;
    }

    public long getId() {
        return id;
    }

    public long getIdSolicitud() {
        return idSolicitud;
    }

    public String getIdPaciente() {
        return idPaciente;
    }

    public long getIdAgenda() {
        return idAgenda;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public int getReprogramaciones() {
        return reprogramaciones;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public String getMotivoCambio() {
        return motivoCambio;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    @Override
    public String toString() {
        return "Cita " + id + " · solicitud " + idSolicitud + " · paciente " + idPaciente + " · " + fechaHora
                + " · agenda " + idAgenda + " · " + estado;
    }
}
