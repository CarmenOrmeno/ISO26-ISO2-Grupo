package es.uclm.esi.iso2.servidor.controlador;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiCitas;
import es.uclm.esi.iso2.servidor.citas.Cita;
import es.uclm.esi.iso2.servidor.citas.ServicioCitas;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

public class ControladorCitas implements ApiCitas {

    private final ServicioCitas citas;
    private final ServicioSolicitudes solicitudes;
    private final ControlAcceso acceso;

    public ControladorCitas(ServicioCitas citas, ServicioSolicitudes solicitudes, ControlAcceso acceso) {
        this.citas = citas;
        this.solicitudes = solicitudes;
        this.acceso = acceso;
    }

    @Override
    public List<SolicitudPrueba> solicitudesPendientes(String token) {
        acceso.exigir(token, Permiso.GESTIONAR_CITAS);
        return solicitudes.pendientes();
    }

    @Override
    public Cita asignar(String token, long idSolicitud, long idAgenda, LocalTime hora) {
        acceso.exigir(token, Permiso.GESTIONAR_CITAS);
        return citas.asignar(idSolicitud, idAgenda, hora);
    }

    @Override
    public Cita reprogramar(String token, long idCita, long idAgenda, LocalTime hora) {
        acceso.exigir(token, Permiso.GESTIONAR_CITAS);
        return citas.reprogramar(idCita, idAgenda, hora);
    }

    @Override
    public void cancelar(String token, long idCita, String motivo) {
        acceso.exigir(token, Permiso.GESTIONAR_CITAS);
        citas.cancelar(idCita, motivo);
    }

    @Override
    public Cita obtener(String token, long idCita) {
        acceso.exigir(token, Permiso.GESTIONAR_CITAS);
        return citas.obtener(idCita);
    }

    @Override
    public List<Cita> delDia(String token, LocalDate fecha) {
        acceso.exigir(token, Permiso.GESTIONAR_CITAS);
        return citas.delDia(fecha);
    }

    @Override
    public List<Cita> dePaciente(String token, String idPaciente) {
        acceso.exigir(token, Permiso.GESTIONAR_CITAS);
        return citas.dePaciente(idPaciente);
    }

    @Override
    public List<Cita> conCambioSolicitado(String token) {
        acceso.exigir(token, Permiso.GESTIONAR_CITAS);
        return citas.conCambioSolicitado();
    }
}
