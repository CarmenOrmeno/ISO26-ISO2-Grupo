package es.uclm.esi.iso2.servidor.notificaciones;

import java.time.LocalDateTime;

import es.uclm.esi.iso2.servidor.comun.Validaciones;

/** Aviso enviado a un paciente; se guarda en su buzón de la aplicación. */
public class Notificacion {

    private final long id;
    private final String idPaciente;
    private final TipoNotificacion tipo;
    private final String mensaje;
    private final LocalDateTime fecha;
    private boolean leida;

    public Notificacion(long id, String idPaciente, TipoNotificacion tipo, String mensaje, LocalDateTime fecha) {
        this.id = id;
        this.idPaciente = Validaciones.texto(idPaciente, "paciente");
        this.tipo = Validaciones.obligatorio(tipo, "tipo");
        this.mensaje = Validaciones.texto(mensaje, "mensaje");
        this.fecha = Validaciones.obligatorio(fecha, "fecha");
    }

    public void marcarLeida() {
        leida = true;
    }

    public long getId() {
        return id;
    }

    public String getIdPaciente() {
        return idPaciente;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public boolean isLeida() {
        return leida;
    }

    @Override
    public String toString() {
        return (leida ? "  " : "* ") + "[" + id + "] " + fecha.toLocalDate() + " " + tipo + ": " + mensaje;
    }
}
