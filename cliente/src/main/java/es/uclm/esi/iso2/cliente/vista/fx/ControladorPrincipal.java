package es.uclm.esi.iso2.cliente.vista.fx;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import es.uclm.esi.iso2.cliente.controlador.Accion;
import es.uclm.esi.iso2.cliente.controlador.Campo;
import es.uclm.esi.iso2.cliente.controlador.Resultado;
import es.uclm.esi.iso2.cliente.controlador.SesionCliente;
import es.uclm.esi.iso2.servidor.seguridad.Usuario;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.util.converter.LocalDateStringConverter;

/**
 * Ventana principal: a la izquierda las acciones del rol agrupadas por caso de uso; a la derecha el formulario
 * de la acción elegida (generado a partir de sus campos) y el resultado.
 */
public class ControladorPrincipal {

    private final SesionCliente sesion;
    private final AplicacionFx aplicacion;
    private final List<Supplier<String>> lectores = new ArrayList<>();
    private Accion actual;

    @FXML
    private Label usuario;
    @FXML
    private TreeView<Object> acciones;
    @FXML
    private Label titulo;
    @FXML
    private GridPane formulario;
    @FXML
    private Button ejecutar;
    @FXML
    private Label estado;
    @FXML
    private ListView<String> resultado;

    ControladorPrincipal(SesionCliente sesion, AplicacionFx aplicacion) {
        this.sesion = sesion;
        this.aplicacion = aplicacion;
    }

    @FXML
    private void initialize() {
        Usuario u = sesion.usuario();
        usuario.setText(u.nombre() + " · " + u.rol());
        TreeItem<Object> raiz = new TreeItem<>("Acciones");
        sesion.accionesPorGrupo().forEach((grupo, lista) -> {
            TreeItem<Object> nodo = new TreeItem<>(grupo);
            lista.forEach(a -> nodo.getChildren().add(new TreeItem<>(a)));
            nodo.setExpanded(true);
            raiz.getChildren().add(nodo);
        });
        acciones.setRoot(raiz);
        acciones.setShowRoot(false);
        acciones.getSelectionModel().selectedItemProperty().addListener((obs, antes, ahora) -> {
            if (ahora != null && ahora.getValue() instanceof Accion accion) {
                seleccionar(accion);
            }
        });
        ejecutar.setDisable(true);
    }

    private void seleccionar(Accion accion) {
        actual = accion;
        titulo.setText(accion.grupo() + " · " + accion.nombre());
        formulario.getChildren().clear();
        lectores.clear();
        for (int i = 0; i < accion.campos().size(); i++) {
            Campo campo = accion.campos().get(i);
            formulario.addRow(i, new Label(campo.etiqueta()), control(campo));
        }
        ejecutar.setDisable(false);
        resultado.getItems().clear();
        estado.setText("");
        if (accion.campos().isEmpty()) {
            ejecutar(); // las consultas sin datos se muestran directamente
        }
    }

    /** Control adecuado al tipo de campo; guarda cómo leer su texto. */
    private Node control(Campo campo) {
        switch (campo.tipo()) {
            case OPCION, SI_NO -> {
                ComboBox<String> lista = new ComboBox<>();
                lista.getItems().setAll(campo.opciones());
                lectores.add(() -> lista.getValue() == null ? "" : lista.getValue());
                return lista;
            }
            case FECHA -> {
                DatePicker fecha = new DatePicker();
                fecha.setConverter(new LocalDateStringConverter(DateTimeFormatter.ISO_LOCAL_DATE, null));
                fecha.setPromptText(campo.tipo().ayuda());
                lectores.add(() -> fecha.getEditor().getText());
                return fecha;
            }
            default -> {
                TextField texto = new TextField();
                texto.setPromptText(campo.tipo().ayuda());
                texto.setPrefColumnCount(28);
                lectores.add(texto::getText);
                return texto;
            }
        }
    }

    @FXML
    private void ejecutar() {
        if (actual == null) {
            return;
        }
        Resultado r = sesion.ejecutar(actual, lectores.stream().map(Supplier::get).toList());
        estado.getStyleClass().removeAll("ok", "error");
        estado.getStyleClass().add(r.correcto() ? "ok" : "error");
        estado.setText((r.correcto() ? "✔ " : "✖ ") + r.mensaje());
        resultado.getItems().setAll(r.lineas());
    }

    @FXML
    private void cerrarSesion() {
        sesion.cerrar();
        aplicacion.mostrarLogin("Sesión cerrada");
    }
}
