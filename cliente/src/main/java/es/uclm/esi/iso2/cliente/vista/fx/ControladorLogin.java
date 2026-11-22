package es.uclm.esi.iso2.cliente.vista.fx;

import es.uclm.esi.iso2.cliente.controlador.SesionCliente;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Pantalla de inicio de sesión (CU01). */
public class ControladorLogin {

    private final SesionCliente sesion;
    private final AplicacionFx aplicacion;
    private final String aviso;

    @FXML
    private TextField usuario;
    @FXML
    private PasswordField contrasena;
    @FXML
    private Label mensaje;

    ControladorLogin(SesionCliente sesion, AplicacionFx aplicacion, String aviso) {
        this.sesion = sesion;
        this.aplicacion = aplicacion;
        this.aviso = aviso;
    }

    @FXML
    private void initialize() {
        mensaje.setText(aviso == null ? "" : aviso);
    }

    @FXML
    private void entrar() {
        try {
            sesion.iniciar(usuario.getText().strip(), contrasena.getText());
            aplicacion.mostrarPrincipal();
        } catch (RuntimeException e) {
            mensaje.getStyleClass().add("error");
            mensaje.setText(e.getMessage());
            contrasena.clear();
        }
    }
}
