package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.Servidor;
import es.uclm.esi.iso2.servidor.api.ApiSesion;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.seguridad.Rol;

class ControladorSesionTest {

    private final Entorno entorno = new Entorno();
    private final ApiSesion api = entorno.servidor.sesion();

    @Test
    @DisplayName("CU01 flujo principal: iniciar sesión, consultar el usuario y cerrar sesión")
    void flujoPrincipal() {
        String token = api.iniciarSesion("medico1", Entorno.CLAVE);
        assertEquals(Rol.MEDICO, api.usuarioActual(token).rol());
        api.cerrarSesion(token);
        assertThrows(AccesoDenegadoException.class, () -> api.usuarioActual(token));
    }

    @Test
    @DisplayName("CU01 flujo alternativo: credenciales incorrectas")
    void credencialesIncorrectas() {
        assertThrows(AccesoDenegadoException.class, () -> api.iniciarSesion("medico1", "otra"));
    }

    @Test
    @DisplayName("Los datos de demostración permiten entrar con los cuatro roles")
    void datosDemo() {
        Servidor demo = Servidor.conDatosDemo();
        for (String login : new String[] { "paciente1", "medico1", "admision1", "admin1" }) {
            String token = demo.sesion().iniciarSesion(login, "demo");
            assertEquals(login, demo.sesion().usuarioActual(token).login());
        }
    }
}
