package es.uclm.esi.iso2.servidor.controlador;

import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiMisCitas;
import es.uclm.esi.iso2.servidor.citas.Cita;
import es.uclm.esi.iso2.servidor.citas.ServicioCitas;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;

public class ControladorMisCitas implements ApiMisCitas {

    private final ServicioCitas citas;
    private final ControlAcceso acceso;

    public ControladorMisCitas(ServicioCitas citas, ControlAcceso acceso) {
        this.citas = citas;
        this.acceso = acceso;
    }

    private String paciente(String token) {
        return acceso.exigir(token, Permiso.GESTIONAR_MIS_CITAS).exigirPaciente();
    }

    @Override
    public List<Cita> misCitas(String token) {
        return citas.dePaciente(paciente(token));
    }

    @Override
    public Cita confirmar(String token, long idCita) {
        return citas.confirmar(idCita, paciente(token));
    }

    @Override
    public Cita solicitarCambio(String token, long idCita, String motivo) {
        return citas.solicitarCambio(idCita, paciente(token), motivo);
    }

    @Override
    public void cancelar(String token, long idCita, String motivo) {
        citas.cancelarPorPaciente(idCita, paciente(token), motivo);
    }
}
