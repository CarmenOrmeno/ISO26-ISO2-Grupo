package es.uclm.esi.iso2.servidor.api;

import java.util.List;

import es.uclm.esi.iso2.servidor.pacientes.DatosPaciente;
import es.uclm.esi.iso2.servidor.pacientes.Paciente;

/** CU02 · Gestionar pacientes (personal de admisión). */
public interface ApiPacientes {

    Paciente alta(String token, DatosPaciente datos);

    Paciente modificar(String token, String idPaciente, DatosPaciente datos);

    void baja(String token, String idPaciente);

    Paciente obtener(String token, String idPaciente);

    /** Lanza NoEncontradoException si no hay ningún paciente con ese DNI. */
    Paciente buscarPorDni(String token, String dni);

    List<Paciente> listarActivos(String token);
}
