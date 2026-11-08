package es.uclm.esi.iso2.servidor;

import es.uclm.esi.iso2.servidor.seguridad.Rol;

/** Datos de demostración para probar el cliente sin sistemas reales. */
final class DatosDemo {

    static final String CONTRASENA = "demo";

    private DatosDemo() {
    }

    static void cargar(Servidor s) {
        s.registrarUsuario("paciente1", CONTRASENA, "Lucía Martín Pérez", Rol.PACIENTE, "PAC-1");
        s.registrarUsuario("medico1", CONTRASENA, "Dr. Javier Ruiz Gómez", Rol.MEDICO, null);
        s.registrarUsuario("admision1", CONTRASENA, "Marta López Sanz", Rol.ADMINISTRATIVO, null);
        s.registrarUsuario("admin1", CONTRASENA, "Pedro García Díaz", Rol.ADMINISTRADOR, null);
    }
}
