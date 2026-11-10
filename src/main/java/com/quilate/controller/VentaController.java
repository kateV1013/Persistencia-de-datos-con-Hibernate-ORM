package com.quilate.controller;

import com.quilate.dao.ClienteDAO;
import com.quilate.dao.PiezaDAO;
import com.quilate.exception.ReglaNegocioException;
import com.quilate.model.Cliente;
import com.quilate.model.DetalleVenta;
import com.quilate.model.EstadoVenta;
import com.quilate.model.Pieza;
import com.quilate.model.Venta;
import com.quilate.service.CalculadoraVenta;
import com.quilate.service.SolicitudDetalleVenta;
import com.quilate.service.VentaService;
import com.quilate.util.AlertUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VentaController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private ComboBox<Pieza> cmbPieza;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtDescuento;
    @FXML private Label lblDescuentoMaximo;
    @FXML private Label lblTotal;
    @FXML private Label lblMensaje;

    @FXML private TableView<LineaCarrito> tablaDetalles;
    @FXML private TableColumn<LineaCarrito, String> colPieza;
    @FXML private TableColumn<LineaCarrito, Integer> colCantidad;
    @FXML private TableColumn<LineaCarrito, BigDecimal> colPrecio;
    @FXML private TableColumn<LineaCarrito, BigDecimal> colDescuento;
    @FXML private TableColumn<LineaCarrito, BigDecimal> colSubtotal;

    @FXML private TableView<Venta> tablaVentas;
    @FXML private TableColumn<Venta, Long> colVentaId;
    @FXML private TableColumn<Venta, String> colCliente;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, BigDecimal> colTotalVenta;
    @FXML private TableColumn<Venta, EstadoVenta> colEstado;

    private final ObservableList<LineaCarrito> carrito = FXCollections.observableArrayList();
    private final VentaService ventaService = new VentaService();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final PiezaDAO piezaDAO = new PiezaDAO();
    private Long ventaEnEdicionId;

    @FXML
    private void initialize() {
        colPieza.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().pieza().getNombre()));
        colCantidad.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().cantidad()));
        colPrecio.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().precio()));
        colDescuento.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().descuento()));
        colSubtotal.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().subtotal()));
        tablaDetalles.setItems(carrito);

        colVentaId.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getId()));
        colCliente.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().getCliente().toString()));
        colFecha.setCellValueFactory(f -> new ReadOnlyStringWrapper(FECHA.format(f.getValue().getFechaHora())));
        colTotalVenta.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getTotal()));
        colEstado.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getEstado()));

        cmbMetodoPago.setItems(FXCollections.observableArrayList(
                "EFECTIVO", "TARJETA", "TRANSFERENCIA"));
        cmbMetodoPago.setValue("EFECTIVO");
        cmbPieza.valueProperty().addListener((obs, anterior, pieza) -> mostrarDescuentoMaximo(pieza));
        tablaVentas.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, venta) -> cargarVenta(venta));
        tablaDetalles.getSelectionModel().selectedItemProperty().addListener((o, a, linea) -> {
            if (linea != null) {
                cmbPieza.setValue(linea.pieza());
                txtCantidad.setText(Integer.toString(linea.cantidad()));
                txtDescuento.setText(linea.descuento().toPlainString());
            }
        });
        recargar();
        nuevaVenta();
    }

    @FXML
    private void agregarDetalle() {
        try {
            Pieza pieza = cmbPieza.getValue();
            if (pieza == null) {
                throw new ReglaNegocioException("Debe seleccionar una pieza.");
            }
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            BigDecimal descuento = decimalOZero(txtDescuento.getText());
            if (cantidad > pieza.getExistencias()) {
                throw new ReglaNegocioException(
                        "Stock insuficiente. Disponible: " + pieza.getExistencias() + ".");
            }


            BigDecimal precio = BigDecimal.valueOf(pieza.getPrecioVenta());
            BigDecimal costo = BigDecimal.valueOf(pieza.getCosto());
            BigDecimal subtotal = CalculadoraVenta.calcularSubtotal(
                    precio, costo, descuento, cantidad);
            carrito.removeIf(linea -> linea.pieza().getId().equals(pieza.getId()));
            carrito.add(new LineaCarrito(pieza, cantidad, precio, descuento, subtotal));
            actualizarTotal();
            txtCantidad.setText("1");
            txtDescuento.setText("0.00");
            mensaje("Pieza agregada.");
        } catch (NumberFormatException e) {
            AlertUtil.advertencia("La cantidad debe ser un numero entero.");
        } catch (RuntimeException e) {
            AlertUtil.error(e);
        }
    }

    @FXML
    private void quitarDetalle() {
        LineaCarrito seleccionada = tablaDetalles.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            carrito.remove(seleccionada);
            actualizarTotal();
        }
    }

    @FXML
    private void guardarPreparacion() {
        try {
            Venta venta = guardar();
            ventaEnEdicionId = venta.getId();
            recargarVentas();
            mensaje("Venta " + venta.getId() + " guardada en preparacion.");
        } catch (RuntimeException e) {
            AlertUtil.error(e);
        }
    }

    @FXML
    private void confirmarVenta() {
        try {
            Venta guardada = guardar();
            ventaEnEdicionId = guardada.getId();
            Venta confirmada = ventaService.confirmar(guardada.getId());
            AlertUtil.info("Venta " + confirmada.getId() + " confirmada. Inventario actualizado.");
            recargar();
            nuevaVenta();
        } catch (RuntimeException e) {
            AlertUtil.error(e);
        }
    }

    @FXML
    private void anularVenta() {
        Venta seleccionada = tablaVentas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            AlertUtil.advertencia("Seleccione una venta confirmada.");
            return;
        }
        if (!AlertUtil.confirmar("Anular la venta " + seleccionada.getId()
                + " y devolver sus existencias?")) {
            return;
        }
        try {
            ventaService.anular(seleccionada.getId());
            recargar();
            nuevaVenta();
            AlertUtil.info("Venta anulada. Las existencias fueron reintegradas una sola vez.");
        } catch (RuntimeException e) {
            AlertUtil.error(e);
        }
    }

    @FXML
    private void nuevaVenta() {
        ventaEnEdicionId = null;
        carrito.clear();
        cmbCliente.setValue(null);
        cmbPieza.setValue(null);
        cmbMetodoPago.setValue("EFECTIVO");
        txtCantidad.setText("1");
        txtDescuento.setText("0.00");
        tablaVentas.getSelectionModel().clearSelection();
        actualizarTotal();
        mensaje("Nueva venta.");
    }

    @FXML
    private void recargar() {
        try {
            cmbCliente.setItems(FXCollections.observableArrayList(clienteDAO.listar()));
            cmbPieza.setItems(FXCollections.observableArrayList(piezaDAO.listarTodas()));
            recargarVentas();
        } catch (RuntimeException e) {
            mensaje("No se pudieron cargar los datos: " + mensajeRaiz(e));
        }
    }

    private Venta guardar() {
        Cliente cliente = cmbCliente.getValue();
        if (cliente == null) {
            throw new ReglaNegocioException("Debe seleccionar un cliente.");
        }
        List<SolicitudDetalleVenta> solicitudes = carrito.stream()
                .map(linea -> new SolicitudDetalleVenta(
                        linea.pieza().getId(), linea.cantidad(), linea.descuento()))
                .toList();
        return ventaEnEdicionId == null
                ? ventaService.crearEnPreparacion(
                        cliente.getId(), cmbMetodoPago.getValue(), solicitudes)
                : ventaService.modificarEnPreparacion(
                        ventaEnEdicionId, cliente.getId(), cmbMetodoPago.getValue(), solicitudes);
    }

    private void cargarVenta(Venta resumen) {
        if (resumen == null) {
            return;
        }
        try {
            Venta venta = ventaService.buscarConDetalles(resumen.getId());
            ventaEnEdicionId = venta.getId();
            cmbCliente.getItems().stream()
                    .filter(c -> c.getId().equals(venta.getCliente().getId()))
                    .findFirst().ifPresent(cmbCliente::setValue);
            cmbMetodoPago.setValue(venta.getMetodoPago());
            carrito.setAll(venta.getDetalles().stream().map(this::aLinea).toList());
            actualizarTotal();
            mensaje(venta.getEstado() == EstadoVenta.PREPARACION
                    ? "Venta en preparacion cargada para editar."
                    : "Venta historica: " + venta.getEstado() + ".");
        } catch (RuntimeException e) {
            AlertUtil.error(e);
        }
    }

    private LineaCarrito aLinea(DetalleVenta detalle) {
        return new LineaCarrito(
                detalle.getPieza(), detalle.getCantidad(), detalle.getPrecioUnitario(),
                detalle.getDescuentoAplicado(), detalle.getSubtotal());
    }

    private void recargarVentas() {
        tablaVentas.setItems(FXCollections.observableArrayList(ventaService.listar()));
    }

    private void mostrarDescuentoMaximo(Pieza pieza) {
        if (pieza == null) {
            lblDescuentoMaximo.setText("Descuento maximo: --");
            return;
        }
        try {
            BigDecimal maximo = CalculadoraVenta.calcularDescuentoMaximo(
                    BigDecimal.valueOf(pieza.getPrecioVenta()), BigDecimal.valueOf(pieza.getCosto()));
            lblDescuentoMaximo.setText("Descuento maximo por unidad: C$ " + maximo);
        } catch (RuntimeException e) {
            lblDescuentoMaximo.setText(e.getMessage());
        }
    }

    private void actualizarTotal() {
        BigDecimal total = CalculadoraVenta.calcularTotal(
                carrito.stream().map(LineaCarrito::subtotal).toList());
        lblTotal.setText("Total: C$ " + total);
    }

    private BigDecimal decimalOZero(String texto) {
        return texto == null || texto.isBlank() ? BigDecimal.ZERO : new BigDecimal(texto.trim());
    }

    private void mensaje(String texto) {
        lblMensaje.setText(texto);
    }

    private String mensajeRaiz(Throwable error) {
        Throwable actual = error;
        while (actual.getCause() != null && actual.getCause() != actual) {
            actual = actual.getCause();
        }
        return actual.getMessage() == null ? actual.getClass().getSimpleName() : actual.getMessage();
    }

    private record LineaCarrito(Pieza pieza, int cantidad, BigDecimal precio,
                                BigDecimal descuento, BigDecimal subtotal) {
    }
}
