package es.uclm.esi.iso2.servidor.pacientes;

import java.util.List;
import java.util.Optional;

/** Contrato interno del componente de pacientes (CU02). */
public interface ServicioPacientes {

    /** Alta de un paciente; el DNI no puede estar repetido. */
    Paciente alta(DatosPaciente datos);

    Paciente modificar(String id, DatosPaciente datos);

    /** Baja lógica. */
    void baja(String id);

    Paciente obtener(String id);

    Optional<Paciente> buscarPorDni(String dni);

    List<Paciente> activos();

    /** Lanza ReglaNegocioException si el paciente no existe o está dado de baja. */
    Paciente obtenerActivo(String id);
}
