package es.uclm.esi.iso2.servidor.integracion;

import java.util.ArrayList;
import java.util.List;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;

/** Sistema de admisión simulado en memoria. */
public class AdmisionSimulado implements SistemaAdmision {

    private final List<PacienteAdmision> pacientes = new ArrayList<>();
    private boolean disponible = true;

    public AdmisionSimulado registrar(PacienteAdmision paciente) {
        pacientes.add(paciente);
        return this;
    }

    @Override
    public List<PacienteAdmision> pacientes() {
        if (!disponible) {
            throw new ReglaNegocioException("Admisión no está disponible");
        }
        return List.copyOf(pacientes);
    }

    @Override
    public boolean disponible() {
        return disponible;
    }

    /** Para pruebas de tolerancia a fallos: simula una caída de admisión. */
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
