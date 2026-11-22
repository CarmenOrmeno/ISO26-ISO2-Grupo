package es.uclm.esi.iso2.servidor.integracion;

import java.time.LocalDate;

/** Datos demográficos de un paciente tal y como los da el sistema de admisión del hospital (CU19). */
public record PacienteAdmision(String dni, String nombre, String apellidos, LocalDate fechaNacimiento,
        String telefono, String email) {
}
