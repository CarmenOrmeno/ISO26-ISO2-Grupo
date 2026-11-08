package es.uclm.esi.iso2.cliente.controlador;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.cliente.vista.Guion;
import es.uclm.esi.iso2.servidor.Servidor;
import es.uclm.esi.iso2.servidor.seguridad.Rol;

class AplicacionClienteTest {

    private String ejecutar(String... lineas) {
        Guion g = new Guion(lineas);
        FabricaMenus.crear(g.consola(), Servidor.conDatosDemo()).ejecutar();
        return g.salida();
    }

    @Test
    @DisplayName("CU01: iniciar sesión, ver el perfil, volver y salir")
    void iniciarSesionYPerfil() {
        String salida = ejecutar("medico1", "demo", "1", "0", "");
        assertTrue(salida.contains("Bienvenido/a, Dr. Javier Ruiz Gómez (MEDICO)"));
        assertTrue(salida.contains("login=medico1"));
        assertTrue(salida.contains("Sesión cerrada"));
        assertTrue(salida.contains("Hasta pronto"));
    }

    @Test
    @DisplayName("CU01: contraseña incorrecta muestra el error y vuelve a pedir usuario")
    void contrasenaIncorrecta() {
        String salida = ejecutar("medico1", "mala", "");
        assertTrue(salida.contains("ERROR: Usuario o contraseña incorrectos"));
    }

    @Test
    @DisplayName("Se acaba la entrada en mitad de un menú: la aplicación termina ordenadamente")
    void finDeEntrada() {
        String salida = ejecutar("admin1", "demo");
        assertTrue(salida.contains("Sesión cerrada"));
        assertTrue(salida.contains("Hasta pronto"));
    }

    @Test
    @DisplayName("Rol sin menú configurado muestra un error")
    void rolSinMenu() {
        Guion g = new Guion("paciente1", "demo", "");
        Servidor s = Servidor.conDatosDemo();
        new AplicacionCliente(g.consola(), s.sesion())
                .conMenu(Rol.MEDICO, new Menu("M", g.consola(), s.sesion())).ejecutar();
        assertTrue(g.salida().contains("No hay menú para el rol PACIENTE"));
    }

    @Test
    @DisplayName("Un error del servidor dentro de una opción se muestra y el menú continúa")
    void errorEnOpcion() {
        Guion g = new Guion("admin1", "demo", "2", "0", "");
        Servidor s = Servidor.conDatosDemo();
        Menu menu = new Menu("M", g.consola(), s.sesion()).agregar("Falla", t -> {
            throw new IllegalStateException("fallo simulado");
        });
        new AplicacionCliente(g.consola(), s.sesion()).conMenu(Rol.ADMINISTRADOR, menu).ejecutar();
        assertTrue(g.salida().contains("ERROR: fallo simulado"));
    }
}
