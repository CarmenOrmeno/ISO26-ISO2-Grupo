package es.uclm.esi.iso2.servidor.integracion;

import java.util.List;

/** Sistema de admisión del hospital (D-009): padrón de pacientes del área. */
public interface SistemaAdmision extends SistemaExterno {

    @Override
    default String nombre() {
        return "Admisión";
    }

    /**
     * Pacientes registrados en admisión.
     *
     * @throws es.uclm.esi.iso2.servidor.comun.ReglaNegocioException si admisión no está disponible
     */
    List<PacienteAdmision> pacientes();
}
