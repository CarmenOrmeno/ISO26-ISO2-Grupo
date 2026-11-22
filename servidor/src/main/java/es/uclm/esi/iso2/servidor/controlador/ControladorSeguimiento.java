package es.uclm.esi.iso2.servidor.controlador;

import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiSeguimiento;
import es.uclm.esi.iso2.servidor.seguimiento.DatosSeguimiento;
import es.uclm.esi.iso2.servidor.seguimiento.Seguimiento;
import es.uclm.esi.iso2.servidor.seguimiento.ServicioSeguimiento;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;

public class ControladorSeguimiento implements ApiSeguimiento {

    private final ServicioSeguimiento seguimientos;
    private final ControlAcceso acceso;

    public ControladorSeguimiento(ServicioSeguimiento seguimientos, ControlAcceso acceso) {
        this.seguimientos = seguimientos;
        this.acceso = acceso;
    }

    private String medico(String token) {
        return acceso.exigir(token, Permiso.PRESCRIBIR_SEGUIMIENTOS).login();
    }

    @Override
    public Seguimiento prescribir(String token, DatosSeguimiento datos) {
        return seguimientos.prescribir(medico(token), datos);
    }

    @Override
    public void cancelar(String token, long idSeguimiento, String motivo) {
        seguimientos.cancelar(idSeguimiento, medico(token), motivo);
    }

    @Override
    public List<Seguimiento> misPrescripciones(String token) {
        return seguimientos.porMedico(medico(token));
    }

    @Override
    public int generarRevisiones(String token) {
        acceso.exigir(token, Permiso.EJECUTAR_TAREAS_PROGRAMADAS);
        return seguimientos.generarRevisionesVencidas();
    }
}
