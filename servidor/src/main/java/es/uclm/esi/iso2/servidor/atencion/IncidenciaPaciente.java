package es.uclm.esi.iso2.servidor.atencion;

import java.io.Serializable;
import java.time.LocalDateTime;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Validaciones;

/**
 * Incidencia, queja o sugerencia que comunica un paciente (CU15). La responde el personal de admisión.
 * Puede referirse a una de sus pruebas ({@code idSolicitud}) o ser general ({@code null}).
 */
public class IncidenciaPaciente implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Longitud máxima de la descripción y de la respuesta. */
    public static final int LONGITUD_MAXIMA = 1000;

    private final long id;
    private final String idPaciente;
    private final CategoriaIncidencia categoria;
    private final String descripcion;
    private final Long idSolicitud;
    private final LocalDateTime fecha;
    private EstadoIncidencia estado = EstadoIncidencia.ABIERTA;
    private String respuesta = "";
    private String respondidaPor = "";
    private LocalDateTime fechaRespuesta;

    public IncidenciaPaciente(long id, String idPaciente, CategoriaIncidencia categoria, String descripcion,
            Long idSolicitud, LocalDateTime fecha) {
        this.id = id;
        this.idPaciente = Validaciones.texto(idPaciente, "paciente");
        this.categoria = Validaciones.obligatorio(categoria, "categoría");
        this.descripcion = textoAcotado(descripcion, "descripción");
        this.idSolicitud = idSolicitud;
        this.fecha = Validaciones.obligatorio(fecha, "fecha");
    }

    /** ABIERTA → RESPONDIDA. Una incidencia solo se responde una vez. */
    public void responder(String respuesta, String login, LocalDateTime cuando) {
        if (estado == EstadoIncidencia.RESPONDIDA) {
            throw new ReglaNegocioException("La incidencia " + id + " ya está respondida");
        }
        this.respuesta = textoAcotado(respuesta, "respuesta");
        this.respondidaPor = Validaciones.texto(login, "login");
        this.fechaRespuesta = Validaciones.obligatorio(cuando, "fecha de respuesta");
        this.estado = EstadoIncidencia.RESPONDIDA;
    }

    private static String textoAcotado(String valor, String campo) {
        String texto = Validaciones.texto(valor, campo);
        if (texto.length() > LONGITUD_MAXIMA) {
            throw new IllegalArgumentException(
                    "El campo «" + campo + "» admite como máximo " + LONGITUD_MAXIMA + " caracteres");
        }
        return texto;
    }

    public long getId() {
        return id;
    }

    public String getIdPaciente() {
        return idPaciente;
    }

    public CategoriaIncidencia getCategoria() {
        return categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** Prueba a la que se refiere, o {@code null} si es una incidencia general. */
    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public EstadoIncidencia getEstado() {
        return estado;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public String getRespondidaPor() {
        return respondidaPor;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    @Override
    public String toString() {
        return "Incidencia " + id + " · " + fecha.toLocalDate() + " · " + categoria
                + (idSolicitud == null ? "" : " · solicitud " + idSolicitud) + " · " + estado + "\n  " + descripcion
                + (estado == EstadoIncidencia.RESPONDIDA ? "\n  Respuesta: " + respuesta : "");
    }
}
