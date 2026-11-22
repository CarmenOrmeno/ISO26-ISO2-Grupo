package es.uclm.esi.iso2.cliente.controlador;

import java.util.ArrayList;
import java.util.List;

import es.uclm.esi.iso2.cliente.vista.Consola;
import es.uclm.esi.iso2.cliente.vista.FinDeEntradaException;
import es.uclm.esi.iso2.servidor.seguridad.Usuario;

/**
 * Controlador de la vista de consola (alternativa a JavaFX, D-017): inicio de sesión (CU01) y menú con las
 * acciones del rol. Usa el mismo {@link SesionCliente} y las mismas {@link Accion} que la vista gráfica.
 */
public class AplicacionCliente {

    private final Consola consola;
    private final SesionCliente sesion;

    public AplicacionCliente(Consola consola, SesionCliente sesion) {
        this.consola = consola;
        this.sesion = sesion;
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
                if (iniciarSesion(login, contrasena)) {
                    try {
                        menu();
                    } finally {
                        sesion.cerrar();
                        consola.mostrar("Sesión cerrada");
                    }
                }
            }
        } catch (FinDeEntradaException e) {
            // fin de la entrada: se sale sin error
        }
        consola.mostrar("Hasta pronto");
    }

    private boolean iniciarSesion(String login, String contrasena) {
        try {
            Usuario u = sesion.iniciar(login, contrasena);
            consola.mostrar("Bienvenido/a, " + u.nombre() + " (" + u.rol() + ")");
            return true;
        } catch (RuntimeException e) {
            consola.error(e.getMessage());
            return false;
        }
    }

    private void menu() {
        List<Accion> acciones = sesion.acciones();
        List<String> textos = acciones.stream().map(a -> a.grupo() + " · " + a.nombre()).toList();
        while (true) {
            int opcion = consola.elegir("Menú de " + sesion.usuario().nombre(), textos);
            if (opcion == 0) {
                return;
            }
            Accion accion = acciones.get(opcion - 1);
            List<String> valores = new ArrayList<>();
            accion.campos().forEach(c -> valores.add(consola.leer(c.etiquetaConAyuda())));
            mostrar(sesion.ejecutar(accion, valores));
        }
    }

    private void mostrar(Resultado r) {
        if (!r.correcto()) {
            consola.error(r.mensaje());
            return;
        }
        consola.mostrar(r.mensaje());
        r.lineas().forEach(l -> consola.mostrar("- " + l));
    }
}
