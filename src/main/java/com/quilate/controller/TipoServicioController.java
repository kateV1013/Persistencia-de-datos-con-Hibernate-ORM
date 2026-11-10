package com.quilate.controller;

import com.quilate.model.TipoServicio;
import com.quilate.service.TipoServicioService;
import com.quilate.util.AlertUtil;
import com.quilate.util.Formatos;
import com.quilate.util.Tareas;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/** Pantalla CRUD de tipos de servicio (grabado, reparacion, ajuste de talla...). */
public class TipoServicioController {

    @FXML private TableView<TipoServicio> tabla;
    @FXML private TableColumn<TipoServicio, String> colNombre;
    @FXML private TableColumn<TipoServicio, String> colDescripcion;
    @FXML private TableColumn<TipoServicio, String> colPrecio;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private Button btnEliminar;
    @FXML private Label lblMensaje;

    private final TipoServicioService service = new TipoServicioService();
    /** Id del servicio que se esta editando, o null si es nuevo. */
    private Integer idEditando;

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colDescripcion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
        colPrecio.setCellValueFactory(c -> new SimpleStringProperty(Formatos.dinero(c.getValue().getPrecioBase())));
        tabla.setPlaceholder(new Label("No hay servicios registrados"));

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, tipo) -> {
            if (tipo != null) {
                mostrar(tipo);
            }
        });

        limpiar();
        cargar();
    }

    private void cargar() {
        Tareas.ejecutar(service::listar,
                lista -> tabla.setItems(FXCollections.observableArrayList(lista)),
                error -> lblMensaje.setText("No se pudieron cargar los servicios: " + Tareas.mensajeCorto(error)));
    }

    private void mostrar(TipoServicio tipo) {
        idEditando = tipo.getId();
        txtNombre.setText(tipo.getNombre());
        txtDescripcion.setText(tipo.getDescripcion());
        txtPrecio.setText(Formatos.dineroEditable(tipo.getPrecioBase()));
        btnEliminar.setDisable(false);
        lblMensaje.setText("Editando: " + tipo.getNombre());
    }

    private void limpiar() {
        idEditando = null;
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        btnEliminar.setDisable(true);
        tabla.getSelectionModel().clearSelection();
        lblMensaje.setText("Nuevo servicio");
    }

    @FXML
    private void onNuevo() {
        limpiar();
        txtNombre.requestFocus();
    }

    @FXML
    private void onGuardar() {
        TipoServicio tipo = new TipoServicio();
        try {
            tipo.setId(idEditando);
            tipo.setNombre(txtNombre.getText());
            tipo.setDescripcion(txtDescripcion.getText());
            tipo.setPrecioBase(Formatos.leerDinero(txtPrecio.getText(), "precio base"));
        } catch (RuntimeException e) {
            Tareas.mostrarError(e);
            return;
        }

        boolean esNuevo = idEditando == null;
        Tareas.ejecutar(() -> service.guardar(tipo), guardado -> {
            AlertUtil.info(esNuevo ? "Servicio registrado." : "Servicio actualizado.");
            limpiar();
            cargar();
        });
    }

    @FXML
    private void onEliminar() {
        if (idEditando == null) {
            return;
        }
        Integer id = idEditando;
        if (!AlertUtil.confirmar("¿Eliminar el servicio \"" + txtNombre.getText() + "\"?")) {
            return;
        }
        Tareas.ejecutar(() -> {
            service.eliminar(id);
            return null;
        }, ok -> {
            limpiar();
            cargar();
        });
    }
}
