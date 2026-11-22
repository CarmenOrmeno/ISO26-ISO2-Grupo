package es.uclm.esi.iso2.cliente.vista.fx;

import java.io.IOException;
import java.io.UncheckedIOException;

import es.uclm.esi.iso2.cliente.controlador.SesionCliente;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Vista gráfica del cliente (D-017). Solo muestra pantallas FXML y pasa lo que hace el usuario a los
 * controladores; la lógica está en {@link SesionCliente} y en las acciones del paquete {@code controlador}.
 */
public class AplicacionFx extends Application {

    private static SesionCliente sesionInicial;
    private static String almacenamientoInicial;

    private Stage ventana;
    private SesionCliente sesion;

    /** Arranca JavaFX con la sesión del cliente (se llama desde {@code Main}). */
    public static void lanzar(SesionCliente sesion, String almacenamiento) {
        sesionInicial = sesion;
        almacenamientoInicial = almacenamiento;
        Application.launch(AplicacionFx.class);
    }

    @Override
    public void start(Stage ventana) {
        this.ventana = ventana;
        this.sesion = sesionInicial;
        ventana.setTitle("Radiodiagnóstico SESCAM");
        mostrarLogin("Datos guardados en: " + almacenamientoInicial);
        ventana.show();
    }

    void mostrarLogin(String aviso) {
        ventana.setScene(escena(cargar("login.fxml", new ControladorLogin(sesion, this, aviso)), 460, 400));
        ventana.centerOnScreen();
    }

    void mostrarPrincipal() {
        ventana.setScene(escena(cargar("principal.fxml", new ControladorPrincipal(sesion, this)), 1150, 720));
        ventana.centerOnScreen();
    }

    private Scene escena(Parent raiz, double ancho, double alto) {
        Scene escena = new Scene(raiz, ancho, alto);
        escena.getStylesheets().add(getClass().getResource("estilo.css").toExternalForm());
        return escena;
    }

    private Parent cargar(String fxml, Object controlador) {
        FXMLLoader cargador = new FXMLLoader(getClass().getResource(fxml));
        cargador.setControllerFactory(tipo -> controlador);
        try {
            return cargador.load();
        } catch (IOException e) {
            throw new UncheckedIOException("No se puede cargar " + fxml, e);
        }
    }

    @Override
    public void stop() {
        sesion.cerrar();
    }
}
