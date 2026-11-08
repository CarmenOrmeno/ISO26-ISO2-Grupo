package es.uclm.esi.iso2.servidor.notificaciones;

import java.util.List;

/** Contrato interno del buzón de notificaciones del paciente (CU12). */
public interface ServicioNotificaciones extends Notificador {

    /** Notificaciones del paciente, de la más reciente a la más antigua. */
    List<Notificacion> dePaciente(String idPaciente);

    long noLeidas(String idPaciente);

    void marcarLeida(long idNotificacion, String idPaciente);
}
