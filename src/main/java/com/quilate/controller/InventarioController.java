package com.quilate.controller;

import com.quilate.dao.CategoriaDAO;
import com.quilate.dao.PiezaDAO;
import com.quilate.exception.EntidadConDependenciasException;
import com.quilate.model.Categoria;
import com.quilate.model.Pieza;
import com.quilate.util.AlertUtil;
import com.quilate.util.Formatos;
import com.quilate.util.ReporteUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Map;
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
    @FXML private TextField  txtMaterial;
    @FXML private TextField  txtQuilates;
    @FXML private TextField  txtPeso;
    @FXML private ComboBox<Categoria> cmbCategoria;
    /** Filtro del reporte por categoría; separado del combo del formulario. */
    @FXML private ComboBox<Categoria> cmbFiltroCategoria;

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
                categorias -> {
                    cmbCategoria.getItems().setAll(categorias);
                    actualizarFiltroCategorias(categorias);
                },
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
        colCosto.setCellFactory(columna -> celdaDinero());
        colPrecio.setCellFactory(columna -> celdaDinero());
        colExistencias.setCellValueFactory(new PropertyValueFactory<>("existencias"));

        // La categoría requiere un cell factory personalizado porque es un objeto
        colCategoria.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCategoria() != null
                        ? cellData.getValue().getCategoria().getNombre()
                        : "Sin categoría"));

        tablaPiezas.setItems(listaPiezas);
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Reportes
    // ──────────────────────────────────────────────────────────────────────

    private void actualizarFiltroCategorias(List<Categoria> categorias) {
        Categoria elegida = cmbFiltroCategoria.getValue();
        cmbFiltroCategoria.getItems().setAll(categorias);
        if (elegida != null) {
            categorias.stream().filter(c -> c.getId().equals(elegida.getId()))
                    .findFirst().ifPresent(cmbFiltroCategoria::setValue);
        }
    }

    @FXML
    public void onReporteGeneral() {
        ReporteUtil.mostrarReporte("/reportes/inventario_general.jrxml", Map.of(), "Inventario general");
    }

    @FXML
    public void onReportePorCategoria() {
        Categoria categoria = cmbFiltroCategoria.getValue();
        if (categoria == null) {
            AlertUtil.advertencia("Elige la categoría a reportar en la lista junto al botón.");
            return;
        }
        ReporteUtil.mostrarReporte("/reportes/inventario_por_categoria.jrxml",
                Map.of("p_categoria_id", categoria.getId()), "Piezas de " + categoria.getNombre());
    }

    @FXML
    public void onReporteStockBajo() {
        TextInputDialog dialogo = new TextInputDialog("3");
        dialogo.setTitle("Stock bajo");
        dialogo.setHeaderText(null);
        dialogo.setContentText("Mostrar piezas con existencias menores o iguales a:");
        var css = getClass().getResource("/css/styles.css");
        if (css != null) dialogo.getDialogPane().getStylesheets().add(css.toExternalForm());
        Optional<String> respuesta = dialogo.showAndWait();
        if (respuesta.isEmpty()) return;
        int limite;
        try {
            limite = Integer.parseInt(respuesta.get().trim());
        } catch (NumberFormatException e) {
            AlertUtil.advertencia("Escribe un número entero, por ejemplo 3.");
            return;
        }
        if (limite < 0) {
            AlertUtil.advertencia("El límite no puede ser negativo.");
            return;
        }
        ReporteUtil.mostrarReporte("/reportes/inventario_stock_bajo.jrxml",
                Map.of("p_stock_minimo", limite), "Stock bajo");
    }

    @FXML
    public void onReporteFicha() {
        Pieza seleccionada = tablaPiezas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            AlertUtil.advertencia("Selecciona una pieza en la tabla para ver su ficha.");
            return;
        }
        ReporteUtil.mostrarReporte("/reportes/inventario_ficha_pieza.jrxml",
                Map.of("p_pieza_id", seleccionada.getId()), "Ficha · " + seleccionada.getNombre());
    }

    /** Muestra un importe Double como "C$ 1,500.00" en lugar de "1500.0". */
    private static TableCell<Pieza, Double> celdaDinero() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null ? null : com.quilate.ui.MonedaVista.formatear(BigDecimal.valueOf(valor)));
            }
        };
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
        actualizarFiltroCategorias(categorias);

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
        try {
            listaPiezas.setAll(piezaDAO.listarTodas());
            mostrarEstado("Inventario cargado: " + listaPiezas.size() + " pieza(s).", false);
        } catch (RuntimeException e) {
            mostrarEstado("No se pudo cargar el inventario: "
                    + com.quilate.util.Tareas.mensajeCorto(e), true);
        }
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

        Optional<Pieza> resultado;
        try {
            resultado = piezaDAO.buscarPorCodigoBarras(codigo);
        } catch (RuntimeException e) {
            mostrarError("No se pudo buscar la pieza: " + com.quilate.util.Tareas.mensajeCorto(e));
            return;
        }

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
                if (piezaDAO.existeCodigoBarras(pieza.getCodigoBarras(), pieza.getId())) {
                    mostrarError("El código de barras ya lo usa otra pieza.");
                    return;
                }
                // Solo se aplica el ajuste de existencias que hizo el usuario; así no se
                // pisan las ventas o apartados registrados desde otra ventana.
                piezaDAO.actualizarDatos(pieza, piezaEnEdicion.getExistencias());
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
        } catch (RuntimeException e) {
            mostrarError("No se pudo crear la categoría: " + com.quilate.util.Tareas.mensajeCorto(e));
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
        } catch (EntidadConDependenciasException | IllegalArgumentException e) {
            mostrarError(e.getMessage());
        } catch (RuntimeException e) {
            mostrarError("No se pudo eliminar la categoría: " + com.quilate.util.Tareas.mensajeCorto(e));
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

        // Costo y precio: se leen igual que en Pedidos y Servicios (acepta "1,500.00" o "C$ 1,500")
        BigDecimal costo = null, precio = null;
        try {
            costo = Formatos.leerDinero(txtCosto.getText(), "costo");
            if (costo.signum() < 0) errores.append("• El costo no puede ser negativo.\n");
        } catch (RuntimeException e) {
            errores.append("• ").append(e.getMessage()).append("\n");
        }
        try {
            precio = Formatos.leerDinero(txtPrecioVenta.getText(), "precio de venta");
        } catch (RuntimeException e) {
            errores.append("• ").append(e.getMessage()).append("\n");
        }

        // Existencias: entero no negativo
        try {
            int existencias = Integer.parseInt(txtExistencias.getText().trim());
            if (existencias < 0) errores.append("• Las existencias no pueden ser negativas.\n");
        } catch (NumberFormatException e) {
            errores.append("• Las existencias deben ser un número entero (ej. 10).\n");
        }

        // Categoría seleccionada
        if (precio != null && precio.signum() <= 0) errores.append("• El precio de venta debe ser mayor que cero.\n");
        if (costo != null && precio != null && precio.compareTo(costo) < 0) {
            errores.append("• El precio de venta no puede ser menor que el costo.\n");
        }
        if (texto(txtMaterial).length() > 50) {
            errores.append("• El material admite como máximo 50 caracteres.\n");
        }
        if (!texto(txtQuilates).isEmpty()) {
            try {
                int quilates = Integer.parseInt(texto(txtQuilates));
                if (quilates < 1 || quilates > 24) errores.append("• Los quilates deben estar entre 1 y 24.\n");
            } catch (NumberFormatException e) {
                errores.append("• Los quilates deben ser un número entero (ej. 18).\n");
            }
        }
        if (!texto(txtPeso).isEmpty()) {
            try {
                BigDecimal peso = new BigDecimal(texto(txtPeso).replace(",", "."));
                if (peso.signum() <= 0 || peso.compareTo(new BigDecimal("999999.99")) > 0) {
                    errores.append("• El peso debe ser mayor que cero (en gramos).\n");
                }
            } catch (NumberFormatException e) {
                errores.append("• El peso debe ser un número (ej. 3.50).\n");
            }
        }
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
        Pieza pieza = new Pieza(
                txtCodigoBarras.getText().trim(),
                txtNombrePieza.getText().trim(),
                Formatos.leerDinero(txtCosto.getText(), "costo").doubleValue(),
                Formatos.leerDinero(txtPrecioVenta.getText(), "precio de venta").doubleValue(),
                Integer.parseInt(txtExistencias.getText().trim()),
                cmbCategoria.getValue()
        );
        pieza.setMaterial(texto(txtMaterial).isEmpty() ? null : texto(txtMaterial));
        pieza.setQuilates(texto(txtQuilates).isEmpty() ? null : Short.valueOf(texto(txtQuilates)));
        pieza.setPesoGramos(texto(txtPeso).isEmpty() ? null
                : new BigDecimal(texto(txtPeso).replace(",", ".")).setScale(2, java.math.RoundingMode.HALF_UP));
        return pieza;
    }

    private static String texto(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    private void cargarPiezaEnFormulario(Pieza pieza) {
        piezaEnEdicion = pieza;
        txtCodigoBarras.setText(pieza.getCodigoBarras());
        txtNombrePieza.setText(pieza.getNombre());
        txtCosto.setText(Formatos.dineroEditable(BigDecimal.valueOf(pieza.getCosto())));
        txtPrecioVenta.setText(Formatos.dineroEditable(BigDecimal.valueOf(pieza.getPrecioVenta())));
        txtExistencias.setText(String.valueOf(pieza.getExistencias()));
        txtMaterial.setText(pieza.getMaterial() == null ? "" : pieza.getMaterial());
        txtQuilates.setText(pieza.getQuilates() == null ? "" : pieza.getQuilates().toString());
        txtPeso.setText(pieza.getPesoGramos() == null ? "" : pieza.getPesoGramos().toPlainString());

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
        txtMaterial.clear();
        txtQuilates.clear();
        txtPeso.clear();
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

    @FXML private void onEtiqueta() {
        Pieza pieza=tablaPiezas.getSelectionModel().getSelectedItem();
        if(pieza==null) { mostrarError("Selecciona una pieza guardada para ver su etiqueta."); return; }
        try { com.quilate.ui.EtiquetaBarras.abrir(tablaPiezas.getScene().getWindow(),pieza); }
        catch(RuntimeException e) { mostrarError(e.getMessage()); }
    }
}
