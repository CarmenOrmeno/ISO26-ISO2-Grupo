package es.uclm.esi.iso2.servidor;

import java.time.LocalDate;

import es.uclm.esi.iso2.servidor.pacientes.DatosPaciente;
import es.uclm.esi.iso2.servidor.seguridad.Rol;

/** Datos de demostración para probar el cliente sin sistemas reales. */
final class DatosDemo {

    static final String CONTRASENA = "demo";

    private DatosDemo() {
    }

    static void cargar(Servidor s) {
        // CU02: el primer paciente recibe el id PAC-1, asociado al usuario paciente1
        s.servicioPacientes().alta(new DatosPaciente("12345678Z", "Lucía", "Martín Pérez",
                LocalDate.of(1980, 5, 14), "600111222", "lucia@example.com"));
        s.servicioPacientes().alta(new DatosPaciente("87654321X", "Antonio", "Gil Moreno",
                LocalDate.of(1955, 9, 3), "925000111", null));
        s.registrarUsuario("paciente1", CONTRASENA, "Lucía Martín Pérez", Rol.PACIENTE, "PAC-1");
        s.registrarUsuario("medico1", CONTRASENA, "Dr. Javier Ruiz Gómez", Rol.MEDICO, null);
        s.registrarUsuario("admision1", CONTRASENA, "Marta López Sanz", Rol.ADMINISTRATIVO, null);
        s.registrarUsuario("admin1", CONTRASENA, "Pedro García Díaz", Rol.ADMINISTRADOR, null);
    }
}
