package com.quilate.controller;

import com.quilate.dao.CategoriaDAO;
import com.quilate.dao.PiezaDAO;
import com.quilate.exception.EntidadConDependenciasException;
import com.quilate.model.Categoria;
import com.quilate.model.Pieza;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador JavaFX para la vista de Inventario.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>CRUD de {@link Pieza} y {@link Categoria}.</li>
 *   <li>Búsqueda por código de barras compatible con lector físico
 *       (el lector escribe el código y envía Enter → se dispara
 *       {@link #onCodigoBarrasBuscar()}).</li>
 *   <li>Validaciones de formulario antes de persistir.</li>
 * </ul>
 * </p>
 *
 * <p>Vista: {@code /fxml/inventario.fxml}, integrada en la ventana principal.</p>
 */
public class InventarioController implements Initializable {

    // ──────────────────────────────────────────────────────────────────────
    //  Inyecciones FXML – Sección "Búsqueda por código de barras"
    // ──────────────────────────────────────────────────────────────────────

    /** Campo de texto donde el lector de barras (o el usuario) escribe el código. */
    @FXML private TextField txtCodigoBarrasBuscar;

    // ──────────────────────────────────────────────────────────────────────
    //  Inyecciones FXML – Tabla de piezas
    // ──────────────────────────────────────────────────────────────────────

    @FXML private TableView<Pieza>            tablaPiezas;
    @FXML private TableColumn<Pieza, Long>    colId;
    @FXML private TableColumn<Pieza, String>  colCodigo;
    @FXML private TableColumn<Pieza, String>  colNombre;
    @FXML private TableColumn<Pieza, Double>  colCosto;
    @FXML private TableColumn<Pieza, Double>  colPrecio;
    @FXML private TableColumn<Pieza, Integer> colExistencias;
    @FXML private TableColumn<Pieza, String>  colCategoria;

    // ──────────────────────────────────────────────────────────────────────
    //  Inyecciones FXML – Formulario de Pieza
    // ──────────────────────────────────────────────────────────────────────

    @FXML private TextField  txtCodigoBarras;
    @FXML private TextField  txtNombrePieza;
    @FXML private TextField  txtCosto;
    @FXML private TextField  txtPrecioVenta;
    @FXML private TextField  txtExistencias;
    @FXML private ComboBox<Categoria> cmbCategoria;

    // ──────────────────────────────────────────────────────────────────────
    //  Inyecciones FXML – Formulario de Categoría
    // ──────────────────────────────────────────────────────────────────────

    @FXML private TextField txtNombreCategoria;
    @FXML private TextField txtDescripcionCategoria;

    // ──────────────────────────────────────────────────────────────────────
    //  Inyecciones FXML – Etiquetas de estado
    // ──────────────────────────────────────────────────────────────────────

    @FXML private Label lblEstado;

    // ──────────────────────────────────────────────────────────────────────
    //  DAOs y estado interno
    // ──────────────────────────────────────────────────────────────────────

    private final PiezaDAO     piezaDAO     = new PiezaDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    /** Pieza actualmente seleccionada para edición (null = alta nueva). */
    private Pieza piezaEnEdicion = null;

    /** Colección observable enlazada a la TableView. */
    private final ObservableList<Pieza> listaPiezas = FXCollections.observableArrayList();

    // ──────────────────────────────────────────────────────────────────────
    //  Inicialización
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnas();
        configurarSeleccionTabla();
        configurarLectorBarras();
        cmbCategoria.valueProperty().addListener((obs, anterior, categoria) -> {
            txtNombreCategoria.setText(categoria == null ? "" : categoria.getNombre());
            txtDescripcionCategoria.setText(categoria == null || categoria.getDescripcion() == null
                    ? "" : categoria.getDescripcion());
        });
        onRecargar();
    }

    @FXML
    public void onRecargar() {
        com.quilate.util.Tareas.ejecutar(categoriaDAO::listarTodas,
                categorias -> cmbCategoria.getItems().setAll(categorias),
                e -> mostrarEstado(com.quilate.util.Tareas.mensajeCorto(e), true));
        com.quilate.util.Tareas.ejecutar(piezaDAO::listarTodas,
                piezas -> {
                    listaPiezas.setAll(piezas);
                    mostrarEstado("Inventario cargado: " + piezas.size() + " pieza(s).", false);
                }, e -> mostrarEstado(com.quilate.util.Tareas.mensajeCorto(e), true));
    }

    @FXML
    public void onActualizarCategoria() {
        Categoria actual = cmbCategoria.getValue();
        if (actual == null) {
            mostrarError("Selecciona una categoría para actualizar.");
            return;
        }
        Categoria editada = new Categoria(txtNombreCategoria.getText().trim(),
                txtDescripcionCategoria.getText().trim());
        editada.setId(actual.getId());
        com.quilate.util.Tareas.ejecutar(() -> categoriaDAO.actualizar(editada),
                resultado -> onRecargar());
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Configuración interna
    // ──────────────────────────────────────────────────────────────────────

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCosto.setCellValueFactory(new PropertyValueFactory<>("costo"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencias.setCellValueFactory(new PropertyValueFactory<>("existencias"));

        // La categoría requiere un cell factory personalizado porque es un objeto
        colCategoria.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCategoria() != null
                        ? cellData.getValue().getCategoria().getNombre()
                        : "Sin categoría"));

        tablaPiezas.setItems(listaPiezas);
    }

    private void configurarSeleccionTabla() {
        tablaPiezas.getSelectionModel()
                   .selectedItemProperty()
                   .addListener((obs, anterior, seleccionada) -> {
                       if (seleccionada != null) {
                           cargarPiezaEnFormulario(seleccionada);
                       }
                   });
    }

    /**
     * Configura el TextField de búsqueda para dispararse al presionar Enter.
     *
     * <p>El lector físico de códigos de barras actúa como un teclado:
     * escribe el código y envía automáticamente una tecla "Enter" al terminar.
     * {@code setOnAction} se dispara exactamente en ese momento.</p>
     */
    private void configurarLectorBarras() {
        // setOnAction en un TextField se dispara cuando se presiona Enter
        txtCodigoBarrasBuscar.setOnAction(event -> onCodigoBarrasBuscar());

        // Opcional: también disparar si el texto tiene longitud de código EAN-13
        // y se detecta un carácter final (algunos lectores no envían Enter)
        txtCodigoBarrasBuscar.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            String texto = txtCodigoBarrasBuscar.getText().trim();
            // EAN-13 tiene 13 dígitos; ajusta según el formato de tu joyería
            if (texto.length() >= 8 && texto.matches("\\d+")) {
                // Si tiene formato numérico y longitud de código, búsqueda automática
                // Descomenta la línea siguiente si tu lector no envía Enter:
                // onCodigoBarrasBuscar();
            }
        });
    }

    private void cargarCategorias() {
        List<Categoria> categorias = categoriaDAO.listarTodas();
        cmbCategoria.setItems(FXCollections.observableArrayList(categorias));

        // Mostrar el nombre en el ComboBox
        cmbCategoria.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombre());
            }
        });
        cmbCategoria.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombre());
            }
        });
    }

    private void cargarTodas() {
        listaPiezas.setAll(piezaDAO.listarTodas());
        mostrarEstado("Inventario cargado: " + listaPiezas.size() + " pieza(s).", false);
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Acción: Búsqueda por código de barras (lector físico)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Busca la pieza cuyo código de barras coincide con el texto en
     * {@link #txtCodigoBarrasBuscar}.
     *
     * <p>Este método se enlaza con {@code setOnAction} del TextField, por lo
     * que se ejecuta cuando el lector envía "Enter" al terminar de escanear.</p>
     */
    @FXML
    public void onCodigoBarrasBuscar() {
        String codigo = txtCodigoBarrasBuscar.getText().trim();
        if (codigo.isEmpty()) {
            cargarTodas();
            return;
        }

        Optional<Pieza> resultado = piezaDAO.buscarPorCodigoBarras(codigo);

        if (resultado.isPresent()) {
            Pieza pieza = resultado.get();
            listaPiezas.setAll(pieza);
            tablaPiezas.getSelectionModel().selectFirst();
            cargarPiezaEnFormulario(pieza);
            mostrarEstado("Pieza encontrada: " + pieza.getNombre(), false);
        } else {
            listaPiezas.clear();
            limpiarFormularioPieza();
            mostrarEstado("No se encontró pieza con código: " + codigo, true);
        }

        // Limpiar el campo de búsqueda y devolver el foco
        // para que el lector pueda escanear el siguiente código
        txtCodigoBarrasBuscar.clear();
        txtCodigoBarrasBuscar.requestFocus();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Acciones CRUD – Piezas
    // ──────────────────────────────────────────────────────────────────────

    /** Guarda una pieza nueva o actualiza la pieza en edición. */
    @FXML
    public void onGuardarPieza() {
        if (!validarFormularioPieza()) return;

        try {
            Pieza pieza = construirPiezaDesdeFormulario();

            if (piezaEnEdicion == null) {
                // Alta nueva: verificar código de barras único
                if (piezaDAO.existeCodigoBarras(pieza.getCodigoBarras(), null)) {
                    mostrarError("El código de barras ya está registrado en otra pieza.");
                    return;
                }
                piezaDAO.guardar(pieza);
                mostrarEstado("Pieza registrada exitosamente.", false);
            } else {
                // Edición
                pieza.setId(piezaEnEdicion.getId());
                pieza.setMaterial(piezaEnEdicion.getMaterial());
                pieza.setQuilates(piezaEnEdicion.getQuilates());
                pieza.setPesoGramos(piezaEnEdicion.getPesoGramos());
                if (piezaDAO.existeCodigoBarras(pieza.getCodigoBarras(), pieza.getId())) {
                    mostrarError("El código de barras ya lo usa otra pieza.");
                    return;
                }
                piezaDAO.actualizar(pieza);
                mostrarEstado("Pieza actualizada exitosamente.", false);
            }

            cargarTodas();
            limpiarFormularioPieza();
            piezaEnEdicion = null;

        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        } catch (Exception e) {
            mostrarError("Error inesperado: " + e.getMessage());
        }
    }

    /** Elimina la pieza seleccionada en la tabla. */
    @FXML
    public void onEliminarPieza() {
        Pieza seleccionada = tablaPiezas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Selecciona una pieza en la tabla para eliminar.");
            return;
        }

        // Confirmación del usuario
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la pieza '" + seleccionada.getNombre() + "'?\n" +
                "Esta acción no se puede deshacer.",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        Optional<ButtonType> respuesta = confirmacion.showAndWait();

        if (respuesta.isEmpty() || respuesta.get() != ButtonType.YES) return;

        try {
            piezaDAO.eliminar(seleccionada.getId());
            mostrarEstado("Pieza eliminada.", false);
            cargarTodas();
            limpiarFormularioPieza();
        } catch (EntidadConDependenciasException e) {
            mostrarError(e.getMessage());
        } catch (Exception e) {
            mostrarError("Error al eliminar: " + e.getMessage());
        }
    }

    /** Limpia el formulario para registrar una pieza nueva. */
    @FXML
    public void onNuevaPieza() {
        piezaEnEdicion = null;
        limpiarFormularioPieza();
        txtCodigoBarras.requestFocus();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Acciones CRUD – Categorías
    // ──────────────────────────────────────────────────────────────────────

    /** Guarda una nueva categoría con los datos del formulario. */
    @FXML
    public void onGuardarCategoria() {
        String nombre = txtNombreCategoria.getText().trim();
        String descripcion = txtDescripcionCategoria.getText().trim();

        if (nombre.isEmpty()) {
            mostrarError("El nombre de la categoría es obligatorio.");
            return;
        }

        try {
            Categoria categoria = new Categoria(nombre, descripcion.isEmpty() ? null : descripcion);
            categoriaDAO.guardar(categoria);
            mostrarEstado("Categoría '" + nombre + "' registrada.", false);
            txtNombreCategoria.clear();
            txtDescripcionCategoria.clear();
            cargarCategorias(); // actualizar el ComboBox
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    /** Elimina la categoría seleccionada en el ComboBox. */
    @FXML
    public void onEliminarCategoria() {
        Categoria seleccionada = cmbCategoria.getValue();
        if (seleccionada == null) {
            mostrarError("Selecciona una categoría del menú desplegable.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la categoría '" + seleccionada.getNombre() + "'?\n" +
                "Solo se puede eliminar si no tiene piezas asociadas.",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        Optional<ButtonType> respuesta = confirmacion.showAndWait();

        if (respuesta.isEmpty() || respuesta.get() != ButtonType.YES) return;

        try {
            categoriaDAO.eliminar(seleccionada.getId());
            mostrarEstado("Categoría eliminada.", false);
            cargarCategorias();
        } catch (EntidadConDependenciasException e) {
            mostrarError(e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Validaciones del formulario de Pieza
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Valida todos los campos del formulario de pieza.
     *
     * <p>Reglas:
     * <ul>
     *   <li>Código de barras: obligatorio, no vacío.</li>
     *   <li>Nombre: obligatorio.</li>
     *   <li>Costo: número decimal válido ≥ 0.</li>
     *   <li>Precio de venta: número decimal válido ≥ 0.</li>
     *   <li>Existencias: número entero no negativo.</li>
     *   <li>Categoría: debe seleccionarse del ComboBox.</li>
     * </ul>
     * </p>
     *
     * @return {@code true} si todos los campos son válidos
     */
    private boolean validarFormularioPieza() {
        StringBuilder errores = new StringBuilder();

        // Código de barras: obligatorio
        String codigo = txtCodigoBarras.getText().trim();
        if (codigo.isEmpty()) {
            errores.append("• El código de barras es obligatorio.\n");
        }

        // Nombre: obligatorio
        if (txtNombrePieza.getText().trim().isEmpty()) {
            errores.append("• El nombre es obligatorio.\n");
        }

        // Costo: decimal no negativo
        try {
            double costo = Double.parseDouble(txtCosto.getText().trim());
            if (!Double.isFinite(costo) || costo < 0) errores.append("• El costo debe ser finito y no negativo.\n");
        } catch (NumberFormatException e) {
            errores.append("• El costo debe ser un número válido (ej. 150.00).\n");
        }

        // Precio de venta: decimal no negativo
        try {
            double precio = Double.parseDouble(txtPrecioVenta.getText().trim());
            if (!Double.isFinite(precio) || precio < 0) errores.append("• El precio debe ser finito y no negativo.\n");
        } catch (NumberFormatException e) {
            errores.append("• El precio de venta debe ser un número válido (ej. 299.99).\n");
        }

        // Existencias: entero no negativo
        try {
            int existencias = Integer.parseInt(txtExistencias.getText().trim());
            if (existencias < 0) errores.append("• Las existencias no pueden ser negativas.\n");
        } catch (NumberFormatException e) {
            errores.append("• Las existencias deben ser un número entero (ej. 10).\n");
        }

        // Categoría seleccionada
        if (cmbCategoria.getValue() == null) {
            errores.append("• Debes seleccionar una categoría.\n");
        }

        if (!errores.isEmpty()) {
            mostrarError(errores.toString());
            return false;
        }
        return true;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Helpers de UI
    // ──────────────────────────────────────────────────────────────────────

    private Pieza construirPiezaDesdeFormulario() {
        return new Pieza(
                txtCodigoBarras.getText().trim(),
                txtNombrePieza.getText().trim(),
                Double.parseDouble(txtCosto.getText().trim()),
                Double.parseDouble(txtPrecioVenta.getText().trim()),
                Integer.parseInt(txtExistencias.getText().trim()),
                cmbCategoria.getValue()
        );
    }

    private void cargarPiezaEnFormulario(Pieza pieza) {
        piezaEnEdicion = pieza;
        txtCodigoBarras.setText(pieza.getCodigoBarras());
        txtNombrePieza.setText(pieza.getNombre());
        txtCosto.setText(String.valueOf(pieza.getCosto()));
        txtPrecioVenta.setText(String.valueOf(pieza.getPrecioVenta()));
        txtExistencias.setText(String.valueOf(pieza.getExistencias()));

        // Seleccionar la categoría correspondiente en el ComboBox
        cmbCategoria.setValue(null);
        if (pieza.getCategoria() != null) cmbCategoria.getItems().stream()
                .filter(c -> c.getId().equals(pieza.getCategoria().getId()))
                .findFirst()
                .ifPresent(cmbCategoria::setValue);
    }

    private void limpiarFormularioPieza() {
        txtCodigoBarras.clear();
        txtNombrePieza.clear();
        txtCosto.clear();
        txtPrecioVenta.clear();
        txtExistencias.clear();
        cmbCategoria.setValue(null);
        tablaPiezas.getSelectionModel().clearSelection();
        piezaEnEdicion = null;
    }

    private void mostrarEstado(String mensaje, boolean esError) {
        if (lblEstado != null) {
            lblEstado.setText(mensaje);
            lblEstado.setStyle(esError ? "-fx-text-fill: #c0392b;" : "-fx-text-fill: #27ae60;");
        }
    }

    private void mostrarError(String mensaje) {
        mostrarEstado(mensaje, true);
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        alert.setTitle("Error de validación");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
