package es.uclm.esi.iso2.cliente.controlador;

import java.util.ArrayList;
import java.util.List;

import es.uclm.esi.iso2.cliente.vista.Consola;
import es.uclm.esi.iso2.cliente.vista.FinDeEntradaException;
import es.uclm.esi.iso2.servidor.api.ApiSesion;

/**
 * Controlador de un menú (MVC): pide a la vista la opción, llama a la API y muestra el resultado.
 * Los errores del servidor se muestran al usuario sin terminar la aplicación.
 */
public class Menu {

    protected final Consola consola;
    private final String titulo;
    private final List<Opcion> opciones = new ArrayList<>();

    public Menu(String titulo, Consola consola, ApiSesion sesion) {
        this.titulo = titulo;
        this.consola = consola;
        agregar("Mi perfil", token -> consola.mostrar(sesion.usuarioActual(token).toString()));
    }

    public final Menu agregar(String texto, java.util.function.Consumer<String> accion) {
        opciones.add(new Opcion(texto, accion));
        return this;
    }

    public List<String> textos() {
        return opciones.stream().map(Opcion::texto).toList();
    }

    /** Ejecuta el menú hasta que el usuario elige 0. */
    public void ejecutar(String token) {
        while (true) {
            int opcion = consola.elegir(titulo, textos());
            if (opcion == 0) {
                return;
            }
            try {
                opciones.get(opcion - 1).accion().accept(token);
            } catch (FinDeEntradaException e) {
                throw e;
            } catch (RuntimeException e) {
                consola.error(e.getMessage());
            }
        }
    }
}
