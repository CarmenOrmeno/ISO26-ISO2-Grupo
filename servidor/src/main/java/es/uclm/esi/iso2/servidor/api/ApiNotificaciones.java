package es.uclm.esi.iso2.servidor.api;

import java.util.List;

import es.uclm.esi.iso2.servidor.notificaciones.Notificacion;

/** CU12 · Notificar y recordar. */
public interface ApiNotificaciones {

    List<Notificacion> misNotificaciones(String token);

    long noLeidas(String token);

    void marcarLeida(String token, long idNotificacion);

    /** Ejecuta la tarea programada de recordatorios (actor Temporizador). Devuelve cuántas citas recordó. */
    int enviarRecordatorios(String token);
}
