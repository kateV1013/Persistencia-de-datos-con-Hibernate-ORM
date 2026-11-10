package com.quilate.controller;

import com.quilate.dao.GenericDAO;
import com.quilate.model.Cliente;
import com.quilate.model.EstadoPedido;
import com.quilate.model.PedidoPersonalizado;
import com.quilate.model.TipoServicio;
import com.quilate.service.PedidoService;
import com.quilate.service.TipoServicioService;
import com.quilate.util.AlertUtil;
import com.quilate.util.Formatos;
import com.quilate.util.Tareas;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Registro de grabados, reparaciones y otros trabajos personalizados:
 * crear, consultar, modificar y cancelar pedidos, mostrando el cupo semanal.
 */
public class PedidoController {

    @FXML private CheckBox chkCancelados;
    @FXML private TableView<PedidoPersonalizado> tabla;
    @FXML private TableColumn<PedidoPersonalizado, String> colId;
    @FXML private TableColumn<PedidoPersonalizado, String> colCliente;
    @FXML private TableColumn<PedidoPersonalizado, String> colServicio;
    @FXML private TableColumn<PedidoPersonalizado, String> colDescripcion;
    @FXML private TableColumn<PedidoPersonalizado, String> colFecha;
    @FXML private TableColumn<PedidoPersonalizado, String> colPrecio;
    @FXML private TableColumn<PedidoPersonalizado, String> colEstado;

    @FXML private Label lblModo;
    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private ComboBox<TipoServicio> cmbServicio;
    @FXML private TextArea txtDescripcion;
    @FXML private DatePicker dpFecha;
    @FXML private Label lblCupo;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<EstadoPedido> cmbEstado;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelarPedido;
    @FXML private Label lblMensaje;

    private final PedidoService pedidoService = new PedidoService();
    private final TipoServicioService tipoServicioService = new TipoServicioService();
    /** Lectura de clientes con el CRUD generico (el CRUD completo es de la pantalla de Clientes). */
    private final GenericDAO<Cliente, Integer> clienteDAO = new GenericDAO<>(Cliente.class) { };

    /** Id del pedido que se esta editando, o null si es nuevo. */
    private Integer idEditando;

    /** Datos que se cargan juntos en segundo plano. */
    private record Datos(List<Cliente> clientes, List<TipoServicio> servicios, List<PedidoPersonalizado> pedidos) {
    }

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCliente().getNombreCompleto()));
        colServicio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoServicio().getNombre()));
        colDescripcion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcionTrabajo()));
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(Formatos.fecha(c.getValue().getFechaProgramada())));
        colPrecio.setCellValueFactory(c -> new SimpleStringProperty(Formatos.dinero(c.getValue().getPrecio())));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().toString()));
        tabla.setPlaceholder(new Label("No hay pedidos registrados"));

        // El estado CANCELADO solo se asigna con el boton "Cancelar pedido"
        cmbEstado.setItems(FXCollections.observableArrayList(
                Arrays.stream(EstadoPedido.values()).filter(EstadoPedido::esActivo).toList()));

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, pedido) -> {
            if (pedido != null) {
                mostrar(pedido);
            }
        });
        dpFecha.setPromptText("dd/mm/aaaa");
        dpFecha.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate fecha) {
                return Formatos.fecha(fecha);
            }

            @Override
            public LocalDate fromString(String texto) {
                try {
                    return texto == null || texto.isBlank() ? null : LocalDate.parse(texto.trim(), Formatos.FECHA);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });
        dpFecha.valueProperty().addListener((obs, anterior, fecha) -> actualizarCupo(fecha));
        cmbServicio.valueProperty().addListener((obs, anterior, tipo) -> {
            // Al crear un pedido se propone el precio base del servicio
            if (tipo != null && idEditando == null) {
                txtPrecio.setText(Formatos.dineroEditable(tipo.getPrecioBase()));
            }
        });

        limpiar();
        cargar();
    }

    // ---------- carga de datos ----------

    private void cargar() {
        boolean incluirCancelados = chkCancelados.isSelected();
        Tareas.ejecutar(() -> new Datos(
                        clienteDAO.listarTodos().stream()
                                .sorted(Comparator.comparing(Cliente::getNombreCompleto, String.CASE_INSENSITIVE_ORDER))
                                .toList(),
                        tipoServicioService.listar(),
                        pedidoService.listar(incluirCancelados)),
                datos -> {
                    Cliente clienteActual = cmbCliente.getValue();
                    TipoServicio servicioActual = cmbServicio.getValue();
                    cmbCliente.setItems(FXCollections.observableArrayList(datos.clientes()));
                    cmbServicio.setItems(FXCollections.observableArrayList(datos.servicios()));
                    cmbCliente.setValue(clienteActual);
                    cmbServicio.setValue(servicioActual);
                    tabla.setItems(FXCollections.observableArrayList(datos.pedidos()));
                },
                error -> lblMensaje.setText("No se pudieron cargar los pedidos: " + Tareas.mensajeCorto(error)));
    }

    private void actualizarCupo(LocalDate fecha) {
        if (fecha == null) {
            lblCupo.setText("");
            return;
        }
        Tareas.ejecutar(() -> pedidoService.ocupadosEnSemana(fecha), ocupados -> {
            if (!fecha.equals(dpFecha.getValue())) {
                return;   // el usuario ya eligio otra fecha
            }
            boolean lleno = ocupados >= PedidoService.CUPO_SEMANAL;
            lblCupo.setText("Semana del " + Formatos.fecha(PedidoService.lunesDe(fecha))
                    + " al " + Formatos.fecha(PedidoService.domingoDe(fecha)) + ": "
                    + ocupados + "/" + PedidoService.CUPO_SEMANAL + " pedidos"
                    + (lleno ? " — sin espacio" : ""));
            lblCupo.getStyleClass().remove("cupo-lleno");
            if (lleno) {
                lblCupo.getStyleClass().add("cupo-lleno");
            }
        }, error -> lblCupo.setText("No se pudo consultar el cupo"));
    }

    // ---------- formulario ----------

    private void mostrar(PedidoPersonalizado p) {
        idEditando = p.getId();
        cmbCliente.setValue(p.getCliente());
        cmbServicio.setValue(p.getTipoServicio());
        txtDescripcion.setText(p.getDescripcionTrabajo());
        dpFecha.setValue(p.getFechaProgramada());
        txtPrecio.setText(Formatos.dineroEditable(p.getPrecio()));

        boolean cancelado = p.getEstado() == EstadoPedido.CANCELADO;
        cmbEstado.setValue(cancelado ? null : p.getEstado());
        cmbEstado.setDisable(cancelado);
        btnGuardar.setDisable(cancelado);
        btnCancelarPedido.setDisable(cancelado || p.getEstado() == EstadoPedido.ENTREGADO);
        lblModo.setText("Pedido N.º " + p.getId());
        lblMensaje.setText(cancelado ? "Este pedido está cancelado (solo lectura)." : "");
    }

    private void limpiar() {
        idEditando = null;
        tabla.getSelectionModel().clearSelection();
        cmbCliente.setValue(null);
        cmbServicio.setValue(null);
        txtDescripcion.clear();
        dpFecha.setValue(null);
        txtPrecio.clear();
        cmbEstado.setValue(EstadoPedido.PENDIENTE);
        cmbEstado.setDisable(true);   // un pedido nuevo siempre empieza PENDIENTE
        btnGuardar.setDisable(false);
        btnCancelarPedido.setDisable(true);
        lblModo.setText("Nuevo pedido");
        lblMensaje.setText("");
    }

    private PedidoPersonalizado leerFormulario() {
        PedidoPersonalizado p = new PedidoPersonalizado();
        p.setId(idEditando);
        p.setCliente(cmbCliente.getValue());
        p.setTipoServicio(cmbServicio.getValue());
        p.setDescripcionTrabajo(txtDescripcion.getText());
        p.setFechaProgramada(dpFecha.getValue());
        p.setPrecio(Formatos.leerDinero(txtPrecio.getText(), "precio"));
        p.setEstado(cmbEstado.getValue());
        return p;
    }

    // ---------- acciones ----------

    @FXML
    private void onRefrescar() {
        limpiar();
        cargar();
    }

    @FXML
    private void onNuevo() {
        limpiar();
        cmbCliente.requestFocus();
    }

    @FXML
    private void onGuardar() {
        PedidoPersonalizado datos;
        try {
            datos = leerFormulario();
        } catch (RuntimeException e) {
            Tareas.mostrarError(e);
            return;
        }

        boolean esNuevo = idEditando == null;
        Tareas.ejecutar(
                () -> esNuevo ? pedidoService.registrar(datos) : pedidoService.modificar(datos),
                guardado -> {
                    AlertUtil.info(esNuevo
                            ? "Pedido N.º " + guardado.getId() + " registrado."
                            : "Pedido N.º " + guardado.getId() + " actualizado.");
                    limpiar();
                    cargar();
                });
    }

    @FXML
    private void onCancelarPedido() {
        if (idEditando == null) {
            return;
        }
        Integer id = idEditando;
        if (!AlertUtil.confirmar("¿Cancelar el pedido N.º " + id + "? Se liberará su espacio en la semana.")) {
            return;
        }
        Tareas.ejecutar(() -> {
            pedidoService.cancelar(id);
            return null;
        }, ok -> {
            AlertUtil.info("Pedido N.º " + id + " cancelado.");
            limpiar();
            cargar();
        });
    }
}
