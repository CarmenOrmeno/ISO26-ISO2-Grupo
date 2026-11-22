package es.uclm.esi.iso2.cliente;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import es.uclm.esi.iso2.cliente.controlador.AplicacionCliente;
import es.uclm.esi.iso2.cliente.controlador.CatalogoAcciones;
import es.uclm.esi.iso2.cliente.controlador.SesionCliente;
import es.uclm.esi.iso2.cliente.vista.Consola;
import es.uclm.esi.iso2.cliente.vista.fx.AplicacionFx;
import es.uclm.esi.iso2.servidor.Servidor;
import es.uclm.esi.iso2.servidor.persistencia.PersistenciaException;

/**
 * Arranque del cliente con el servidor en proceso (comunicación simulada, D-007). Por defecto abre la vista
 * JavaFX (D-017); con {@code --consola}, la vista de consola. No extiende {@code Application} para que el jar
 * ejecutable arranque JavaFX desde el classpath.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Servidor servidor;
        try {
            servidor = Servidor.conDatosDemo();
        } catch (PersistenciaException e) {
            System.err.println(mensajeSinBaseDeDatos(e));
            System.exit(1);
            return;
        }
        SesionCliente sesion = CatalogoAcciones.sesion(servidor);
        if (List.of(args).contains("--consola")) {
            PrintStream salida = new PrintStream(System.out, true, StandardCharsets.UTF_8);
            Consola consola = new Consola(
                    new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)), salida);
            salida.println("Datos guardados en: " + servidor.almacenamiento());
            new AplicacionCliente(consola, sesion).ejecutar();
        } else {
            AplicacionFx.lanzar(sesion, servidor.almacenamiento());
        }
    }

    /** Mensaje para el usuario cuando MySQL está configurado (RADIO_DB_URL) pero no se puede usar. */
    static String mensajeSinBaseDeDatos(PersistenciaException e) {
        Throwable causa = e.getCause() == null ? e : e.getCause();
        String detalle = causa.getMessage() == null ? causa.getClass().getSimpleName()
                : causa.getMessage().lines().findFirst().orElse("");
        return "No se puede arrancar: " + e.getMessage() + " (" + detalle + ").\n"
                + "Comprueba que MySQL está en marcha y que RADIO_DB_URL, RADIO_DB_USUARIO y RADIO_DB_CLAVE son "
                + "correctas, o quita RADIO_DB_URL para guardar los datos en memoria.";
    }
}
