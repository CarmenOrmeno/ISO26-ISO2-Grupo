package es.uclm.esi.iso2.servidor.seguimiento;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Validaciones;
import es.uclm.esi.iso2.servidor.solicitudes.DatosSolicitud;

/**
 * Prueba de seguimiento periódica prescrita por un médico (CU10). Cada revisión vencida genera una
 * nueva solicitud de prueba; al generar la última, el seguimiento termina.
 */
public class Seguimiento implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int PERIODICIDAD_MINIMA = 1;
    public static final int PERIODICIDAD_MAXIMA = 60;
    public static final int REVISIONES_MINIMAS = 1;
    public static final int REVISIONES_MAXIMAS = 20;

    private final long id;
    private final String loginMedico;
    private final String idPaciente;
    private final String codigoTipo;
    private final String indicacionClinica;
    private final String justificacion;
    private final Prioridad prioridad;
    private final int periodicidadMeses;
    private final int numeroRevisiones;
    private final LocalDate primeraRevision;
    private final List<Long> solicitudes = new ArrayList<>();
    private EstadoSeguimiento estado = EstadoSeguimiento.ACTIVO;
    private String motivoCancelacion = "";

    public Seguimiento(long id, String loginMedico, DatosSeguimiento datos) {
        this.id = id;
        this.loginMedico = Validaciones.texto(loginMedico, "médico");
        Validaciones.obligatorio(datos, "datos");
        this.idPaciente = Validaciones.texto(datos.idPaciente(), "paciente");
        this.codigoTipo = Validaciones.texto(datos.codigoTipo(), "tipo de prueba");
        this.indicacionClinica = Validaciones.texto(datos.indicacionClinica(), "indicación clínica");
        this.justificacion = Validaciones.texto(datos.justificacion(), "justificación");
        this.prioridad = Validaciones.obligatorio(datos.prioridad(), "prioridad");
        this.periodicidadMeses = Validaciones.rango(datos.periodicidadMeses(), PERIODICIDAD_MINIMA,
                PERIODICIDAD_MAXIMA, "periodicidad (meses)");
        this.numeroRevisiones = Validaciones.rango(datos.numeroRevisiones(), REVISIONES_MINIMAS, REVISIONES_MAXIMAS,
                "número de revisiones");
        this.primeraRevision = Validaciones.obligatorio(datos.primeraRevision(), "primera revisión");
    }

    /** Fecha de la siguiente revisión que falta por generar; vacía si el seguimiento no está activo. */
    public Optional<LocalDate> proximaRevision() {
        if (estado != EstadoSeguimiento.ACTIVO) {
            return Optional.empty();
        }
        return Optional.of(primeraRevision.plusMonths((long) periodicidadMeses * solicitudes.size()));
    }

    /** La revisión pendiente vence hoy o ya ha vencido. */
    public boolean tieneRevisionVencida(LocalDate hoy) {
        return proximaRevision().map(fecha -> !fecha.isAfter(hoy)).orElse(false);
    }

    /** Datos de la solicitud que genera la próxima revisión. */
    public DatosSolicitud datosRevision() {
        return new DatosSolicitud(idPaciente, codigoTipo, indicacionClinica,
                justificacion + " (seguimiento " + id + ", revisión " + (solicitudes.size() + 1) + "/"
                        + numeroRevisiones + ")",
                prioridad);
    }

    /** Anota la solicitud generada para la revisión pendiente; con la última, el seguimiento FINALIZA. */
    public void registrarRevision(long idSolicitud) {
        exigirActivo("generar revisiones de");
        solicitudes.add(idSolicitud);
        if (solicitudes.size() == numeroRevisiones) {
            estado = EstadoSeguimiento.FINALIZADO;
        }
    }

    /** ACTIVO → CANCELADO. Las solicitudes ya generadas no se tocan. */
    public void cancelar(String motivo) {
        exigirActivo("cancelar");
        motivoCancelacion = Validaciones.texto(motivo, "motivo");
        estado = EstadoSeguimiento.CANCELADO;
    }

    private void exigirActivo(String operacion) {
        if (estado != EstadoSeguimiento.ACTIVO) {
            throw new ReglaNegocioException("No se puede " + operacion + " el seguimiento " + id + " en estado "
                    + estado);
        }
    }

    public long getId() {
        return id;
    }

    public String getLoginMedico() {
        return loginMedico;
    }

    public String getIdPaciente() {
        return idPaciente;
    }

    public String getCodigoTipo() {
        return codigoTipo;
    }

    public String getIndicacionClinica() {
        return indicacionClinica;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public int getPeriodicidadMeses() {
        return periodicidadMeses;
    }

    public int getNumeroRevisiones() {
        return numeroRevisiones;
    }

    public LocalDate getPrimeraRevision() {
        return primeraRevision;
    }

    /** Ids de las solicitudes generadas, en orden. */
    public List<Long> getSolicitudes() {
        return Collections.unmodifiableList(solicitudes);
    }

    public int revisionesGeneradas() {
        return solicitudes.size();
    }

    public EstadoSeguimiento getEstado() {
        return estado;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    @Override
    public String toString() {
        return "Seguimiento " + id + " · " + codigoTipo + " · paciente " + idPaciente + " · cada "
                + periodicidadMeses + " meses · " + solicitudes.size() + "/" + numeroRevisiones + " revisiones · "
                + estado + proximaRevision().map(f -> " · próxima " + f).orElse("");
    }
}
