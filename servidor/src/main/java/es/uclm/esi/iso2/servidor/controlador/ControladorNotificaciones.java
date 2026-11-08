package es.uclm.esi.iso2.servidor.controlador;

import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiNotificaciones;
import es.uclm.esi.iso2.servidor.notificaciones.Notificacion;
import es.uclm.esi.iso2.servidor.notificaciones.ServicioNotificaciones;
import es.uclm.esi.iso2.servidor.notificaciones.ServicioRecordatorios;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;

public class ControladorNotificaciones implements ApiNotificaciones {

    private final ServicioNotificaciones notificaciones;
    private final ServicioRecordatorios recordatorios;
    private final ControlAcceso acceso;

    public ControladorNotificaciones(ServicioNotificaciones notificaciones, ServicioRecordatorios recordatorios,
            ControlAcceso acceso) {
        this.notificaciones = notificaciones;
        this.recordatorios = recordatorios;
        this.acceso = acceso;
    }

    private String paciente(String token) {
        return acceso.exigir(token, Permiso.CONSULTAR_MIS_NOTIFICACIONES).exigirPaciente();
    }

    @Override
    public List<Notificacion> misNotificaciones(String token) {
        return notificaciones.dePaciente(paciente(token));
    }

    @Override
    public long noLeidas(String token) {
        return notificaciones.noLeidas(paciente(token));
    }

    @Override
    public void marcarLeida(String token, long idNotificacion) {
        notificaciones.marcarLeida(idNotificacion, paciente(token));
    }

    @Override
    public int enviarRecordatorios(String token) {
        acceso.exigir(token, Permiso.EJECUTAR_TAREAS_PROGRAMADAS);
        return recordatorios.enviarRecordatorios();
    }
}
