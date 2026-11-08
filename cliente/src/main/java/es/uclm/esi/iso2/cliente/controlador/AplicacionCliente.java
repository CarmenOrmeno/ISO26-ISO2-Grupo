package es.uclm.esi.iso2.cliente.controlador;

import java.util.EnumMap;
import java.util.Map;

import es.uclm.esi.iso2.cliente.vista.Consola;
import es.uclm.esi.iso2.cliente.vista.FinDeEntradaException;
import es.uclm.esi.iso2.servidor.api.ApiSesion;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.seguridad.Rol;

/** Controlador principal del cliente: inicio de sesión (CU01) y menú según el rol. */
public class AplicacionCliente {

    private final Consola consola;
    private final ApiSesion sesion;
    private final Map<Rol, Menu> menus = new EnumMap<>(Rol.class);

    public AplicacionCliente(Consola consola, ApiSesion sesion) {
        this.consola = consola;
        this.sesion = sesion;
    }

    public AplicacionCliente conMenu(Rol rol, Menu menu) {
        menus.put(rol, menu);
        return this;
    }

    /** Bucle principal: hasta que el usuario deja el login vacío o se acaba la entrada. */
    public void ejecutar() {
        consola.titulo("Radiodiagnóstico SESCAM");
        try {
            while (true) {
                String login = consola.leer("Usuario (vacío para salir)");
                if (login.isEmpty()) {
                    break;
                }
                String contrasena = consola.leer("Contraseña");
                iniciarSesion(login, contrasena);
            }
        } catch (FinDeEntradaException e) {
            // fin de la entrada: se sale sin error
        }
        consola.mostrar("Hasta pronto");
    }

    private void iniciarSesion(String login, String contrasena) {
        String token;
        try {
            token = sesion.iniciarSesion(login, contrasena);
        } catch (AccesoDenegadoException e) {
            consola.error(e.getMessage());
            return;
        }
        try {
            Rol rol = sesion.usuarioActual(token).rol();
            consola.mostrar("Bienvenido/a, " + sesion.usuarioActual(token).nombre() + " (" + rol + ")");
            Menu menu = menus.get(rol);
            if (menu == null) {
                consola.error("No hay menú para el rol " + rol);
            } else {
                menu.ejecutar(token);
            }
        } finally {
            sesion.cerrarSesion(token);
            consola.mostrar("Sesión cerrada");
        }
    }
}
