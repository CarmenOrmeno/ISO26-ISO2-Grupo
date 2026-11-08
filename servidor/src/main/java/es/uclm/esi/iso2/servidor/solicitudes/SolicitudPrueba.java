package es.uclm.esi.iso2.servidor.solicitudes;

import java.time.LocalDateTime;
import java.util.Arrays;

import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Validaciones;

/** Solicitud de una prueba diagnóstica hecha por un médico (CU04) o generada por un seguimiento (CU10). */
public class SolicitudPrueba {

    private final long id;
    private final String idPaciente;
    private final String loginMedico;
    private final String codigoTipo;
    private final String indicacionClinica;
    private final String justificacion;
    private final LocalDateTime fechaSolicitud;
    private final Long idSeguimiento;
    private Prioridad prioridad;
    private EstadoSolicitud estado = EstadoSolicitud.SOLICITADA;
    private LocalDateTime fechaRealizacion;
    private String observaciones = "";
    private String motivoCancelacion = "";

    public SolicitudPrueba(long id, String loginMedico, DatosSolicitud datos, LocalDateTime fechaSolicitud,
            Long idSeguimiento) {
        this.id = id;
        this.loginMedico = Validaciones.texto(loginMedico, "médico");
        Validaciones.obligatorio(datos, "datos");
        this.idPaciente = Validaciones.texto(datos.idPaciente(), "paciente");
        this.codigoTipo = Validaciones.texto(datos.codigoTipo(), "tipo de prueba");
        this.indicacionClinica = Validaciones.texto(datos.indicacionClinica(), "indicación clínica");
        this.justificacion = Validaciones.texto(datos.justificacion(), "justificación");
        this.prioridad = Validaciones.obligatorio(datos.prioridad(), "prioridad");
        this.fechaSolicitud = Validaciones.obligatorio(fechaSolicitud, "fecha de solicitud");
        this.idSeguimiento = idSeguimiento;
    }

    private void exigirEstado(String operacion, EstadoSolicitud... permitidos) {
        if (Arrays.stream(permitidos).noneMatch(e -> e == estado)) {
            throw new ReglaNegocioException("No se puede " + operacion + " la solicitud " + id
                    + " en estado " + estado);
        }
    }

    /** SOLICITADA → CITADA (CU06). */
    public void citar() {
        exigirEstado("citar", EstadoSolicitud.SOLICITADA);
        estado = EstadoSolicitud.CITADA;
    }

    /** CITADA → SOLICITADA: se cancela la cita y la solicitud vuelve a la lista de espera. */
    public void anularCita() {
        exigirEstado("anular la cita de", EstadoSolicitud.CITADA);
        estado = EstadoSolicitud.SOLICITADA;
    }

    /** CITADA → REALIZADA (CU08). */
    public void registrarRealizacion(LocalDateTime fecha, String obs) {
        exigirEstado("registrar la realización de", EstadoSolicitud.CITADA);
        fechaRealizacion = Validaciones.obligatorio(fecha, "fecha de realización");
        observaciones = obs == null ? "" : obs.strip();
        estado = EstadoSolicitud.REALIZADA;
    }

    /** REALIZADA → INFORMADA (CU09). */
    public void informar() {
        exigirEstado("informar", EstadoSolicitud.REALIZADA);
        estado = EstadoSolicitud.INFORMADA;
    }

    /** SOLICITADA → CANCELADA. Si está citada hay que cancelar antes la cita. */
    public void cancelar(String motivo) {
        exigirEstado("cancelar", EstadoSolicitud.SOLICITADA);
        motivoCancelacion = Validaciones.texto(motivo, "motivo");
        estado = EstadoSolicitud.CANCELADA;
    }

    /** Solo se puede repriorizar mientras espera cita (CU11). */
    public void cambiarPrioridad(Prioridad nueva) {
        exigirEstado("cambiar la prioridad de", EstadoSolicitud.SOLICITADA);
        prioridad = Validaciones.obligatorio(nueva, "prioridad");
    }

    public long getId() {
        return id;
    }

    public String getIdPaciente() {
        return idPaciente;
    }

    public String getLoginMedico() {
        return loginMedico;
    }

    public String getCodigoTipo() {
        return codigoTipo;
    }

    public String getIndicacionClinica() {
        return indicacionClinica;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public Long getIdSeguimiento() {
        return idSeguimiento;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public LocalDateTime getFechaRealizacion() {
        return fechaRealizacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    @Override
    public String toString() {
        return "Solicitud " + id + " · " + codigoTipo + " · paciente " + idPaciente + " · " + prioridad + " · "
                + estado + " · " + fechaSolicitud.toLocalDate();
    }
}
