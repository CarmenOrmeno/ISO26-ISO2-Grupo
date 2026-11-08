package es.uclm.esi.iso2.servidor.pacientes;

import java.time.LocalDate;

/** Datos de entrada para dar de alta o modificar un paciente. */
public record DatosPaciente(String dni, String nombre, String apellidos, LocalDate fechaNacimiento,
        String telefono, String email) {
}
