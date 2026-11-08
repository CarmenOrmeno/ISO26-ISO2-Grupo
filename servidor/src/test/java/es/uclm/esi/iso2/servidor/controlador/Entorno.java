package es.uclm.esi.iso2.servidor.controlador;

import java.time.LocalDateTime;

import es.uclm.esi.iso2.servidor.Servidor;
import es.uclm.esi.iso2.servidor.comun.RelojAjustable;
import es.uclm.esi.iso2.servidor.seguridad.Rol;

/** Servidor completo para las pruebas de integración de los controladores. Lunes 02/11/2026 9:00. */
class Entorno {

    static final String CLAVE = "x";

    final RelojAjustable reloj = new RelojAjustable(LocalDateTime.of(2026, 11, 2, 9, 0));
    final Servidor servidor = new Servidor(reloj);

    Entorno() {
        servidor.registrarUsuario("paciente1", CLAVE, "Lucía Martín", Rol.PACIENTE, "PAC-1");
        servidor.registrarUsuario("medico1", CLAVE, "Dr. Ruiz", Rol.MEDICO, null);
        servidor.registrarUsuario("medico2", CLAVE, "Dra. Sanz", Rol.MEDICO, null);
        servidor.registrarUsuario("admision1", CLAVE, "Marta López", Rol.ADMINISTRATIVO, null);
        servidor.registrarUsuario("admin1", CLAVE, "Pedro García", Rol.ADMINISTRADOR, null);
    }

    String token(String login) {
        return servidor.sesion().iniciarSesion(login, CLAVE);
    }
}
