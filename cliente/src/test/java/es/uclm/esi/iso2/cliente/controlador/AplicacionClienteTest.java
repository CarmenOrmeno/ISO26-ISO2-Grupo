package es.uclm.esi.iso2.cliente.controlador;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.cliente.vista.Guion;

/** Sistema: la vista de consola con entrada simulada. */
class AplicacionClienteTest {

    private String ejecutar(String... lineas) {
        Guion g = new Guion(lineas);
        new AplicacionCliente(g.consola(), CatalogoAcciones.sesion(Demo.servidor())).ejecutar();
        return g.salida();
    }

    @Test
    @DisplayName("CU01: iniciar sesión, ver el perfil, volver y salir")
    void iniciarSesionYPerfil() {
        String salida = ejecutar("medico1", "demo", "1", "0", "");
        assertTrue(salida.contains("Bienvenido/a, Dr. Javier Ruiz Gómez (MEDICO)"));
        assertTrue(salida.contains("1. CU01 Mi cuenta · Mi perfil"));
        assertTrue(salida.contains("login=medico1"));
        assertTrue(salida.contains("Sesión cerrada"));
        assertTrue(salida.contains("Hasta pronto"));
    }

    @Test
    @DisplayName("CU01: contraseña incorrecta muestra el error y vuelve a pedir usuario")
    void contrasenaIncorrecta() {
        assertTrue(ejecutar("medico1", "mala", "").contains("ERROR: Usuario o contraseña incorrectos"));
    }

    @Test
    @DisplayName("Una acción con campos: los pide con su formato y muestra el resultado (CU02)")
    void accionConCampos() {
        // admisión: 1 perfil, 2 pacientes activos, 3 buscar por DNI
        String salida = ejecutar("admision1", "demo", "3", "12345678Z", "0", "");
        assertTrue(salida.contains("DNI (texto): "));
        assertTrue(salida.contains("Lucía"));
    }

    @Test
    @DisplayName("Un valor mal escrito muestra el error y el menú continúa")
    void valorInvalido() {
        // admisión: 8 agendas de un día (1 perfil, 2–7 pacientes)
        String salida = ejecutar("admision1", "demo", "8", "ayer", "0", "");
        assertTrue(salida.contains("ERROR: «ayer» no es válido en «Fecha» (AAAA-MM-DD)"));
        assertTrue(salida.contains("Sesión cerrada"));
    }

    @Test
    @DisplayName("Se acaba la entrada en mitad de un menú: la aplicación termina ordenadamente")
    void finDeEntrada() {
        String salida = ejecutar("admin1", "demo");
        assertTrue(salida.contains("Sesión cerrada"));
        assertTrue(salida.contains("Hasta pronto"));
    }
}
