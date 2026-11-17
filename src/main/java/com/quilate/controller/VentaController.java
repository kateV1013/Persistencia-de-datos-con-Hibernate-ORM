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
import com.quilate.util.Tareas;
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
    @FXML private TextField txtCodigoVenta;
    @FXML private Label lblGanancia, lblMargen, lblAhorro;
    private boolean ventaHistorica;

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
    /** Evita lanzar dos guardados o confirmaciones a la vez con un doble clic. */
    private boolean ocupado;

    /** Datos que la pantalla necesita de la BD; se cargan fuera del hilo de JavaFX. */
    private record Datos(List<Cliente> clientes, List<Pieza> piezas, List<Venta> ventas) {
    }

    /** Lo que hay que guardar, leído del formulario antes de ir a la BD. */
    private record Solicitud(Long ventaId, Long clienteId, String metodoPago,
                             List<SolicitudDetalleVenta> detalles) {
    }

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
    private void escanearPieza() {
        if (ventaHistorica) {
            mensaje("Esta venta es histórica. Pulsa Nueva venta para registrar otra.");
            return;
        }
        String codigo = txtCodigoVenta.getText();
        Tareas.ejecutar(() -> new com.quilate.service.EscaneoService().buscar(codigo), pieza -> {
            try {
                LineaCarrito anterior = carrito.stream().filter(l -> l.pieza().getId().equals(pieza.getId())).findFirst().orElse(null);
                int cantidad = com.quilate.service.EscaneoService.cantidadTrasLectura(anterior==null?0:anterior.cantidad(), pieza.getExistencias());
                BigDecimal precio=BigDecimal.valueOf(pieza.getPrecioVenta()), costo=BigDecimal.valueOf(pieza.getCosto());
                BigDecimal descuento=anterior==null?BigDecimal.ZERO:anterior.descuento();
                BigDecimal subtotal=CalculadoraVenta.calcularSubtotal(precio,costo,descuento,cantidad);
                LineaCarrito nueva = new LineaCarrito(pieza,cantidad,precio,costo,descuento,subtotal);
                if (anterior==null) carrito.add(nueva); else carrito.set(carrito.indexOf(anterior),nueva);
                cmbPieza.setValue(pieza); actualizarTotal();
                mensaje("Lectura correcta · " + pieza.getNombre() + " · " + cantidad + " unidad(es) en la venta.");
                txtCodigoVenta.clear();
            } catch (RuntimeException e) { mensaje(e.getMessage()); txtCodigoVenta.selectAll(); }
            txtCodigoVenta.requestFocus();
        }, error -> {
            mensaje(Tareas.mensajeCorto(error));
            txtCodigoVenta.selectAll();
            txtCodigoVenta.requestFocus();
        });
    }

    @FXML
    private void agregarDetalle() {
        if (ventaHistorica) { mensaje("Pulsa Nueva venta para registrar otra; la venta histórica no se modifica."); return; }
        try {
            Pieza pieza = cmbPieza.getValue();
            if (pieza == null) {
                throw new ReglaNegocioException("Debe seleccionar una pieza.");
            }
            int cantidad;
            try {
                cantidad = Integer.parseInt(txtCantidad.getText().trim());
            } catch (NumberFormatException e) {
                throw new ReglaNegocioException("La cantidad debe ser un número entero.");
            }
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
            carrito.add(new LineaCarrito(pieza, cantidad, precio, costo, descuento, subtotal));
            actualizarTotal();
            txtCantidad.setText("1");
            txtDescuento.setText("0.00");
            mensaje("Pieza agregada.");
        } catch (RuntimeException e) {
            Tareas.mostrarError(e);
        }
    }

    @FXML
    private void quitarDetalle() {
        if (ventaHistorica) { mensaje("Las piezas de una venta histórica no se pueden quitar."); return; }
        LineaCarrito seleccionada = tablaDetalles.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            carrito.remove(seleccionada);
            actualizarTotal();
        }
    }

    @FXML
    private void guardarPreparacion() {
        Solicitud solicitud = leerSolicitud();
        if (solicitud == null) return;
        ejecutarOcupado(() -> guardar(solicitud), venta -> {
            ventaEnEdicionId = venta.getId();
            recargarVentas();
            mensaje("Venta " + venta.getId() + " guardada en preparacion.");
        });
    }

    @FXML
    private void eliminarBorrador() {
        if (ventaEnEdicionId == null || ventaHistorica) {
            AlertUtil.advertencia("Selecciona una venta en preparación para eliminarla.");
            return;
        }
        Long id = ventaEnEdicionId;
        if (!AlertUtil.confirmar("¿Eliminar el borrador de la venta " + id + "? No afecta las existencias.")) {
            return;
        }
        ejecutarOcupado(() -> { ventaService.eliminarBorrador(id); return id; }, eliminado -> {
            nuevaVenta();
            recargarVentas();
            mensaje("Borrador de la venta " + eliminado + " eliminado.");
        });
    }

    @FXML
    private void confirmarVenta() {
        Solicitud solicitud = leerSolicitud();
        if (solicitud == null) return;
        // Primero se guarda el borrador: si la confirmación falla, el siguiente intento
        // reutiliza esa misma venta en lugar de crear otra.
        ejecutarOcupado(() -> guardar(solicitud), guardada -> {
            ventaEnEdicionId = guardada.getId();
            ejecutarOcupado(() -> ventaService.confirmar(guardada.getId()), confirmada -> {
                AlertUtil.info("Venta " + confirmada.getId() + " confirmada. Inventario actualizado.");
                recargar();
                nuevaVenta();
            });
        });
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
        Long id = seleccionada.getId();
        ejecutarOcupado(() -> ventaService.anular(id), anulada -> {
            recargar();
            nuevaVenta();
            AlertUtil.info("Venta anulada. Las existencias fueron reintegradas una sola vez.");
        });
    }

    @FXML
    private void nuevaVenta() {
        ventaHistorica = false;
        txtCodigoVenta.clear();
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
        Tareas.ejecutar(() -> new Datos(clienteDAO.listar(), piezaDAO.listarTodas(), ventaService.listar()),
                datos -> {
                    Cliente cliente = cmbCliente.getValue();
                    Pieza pieza = cmbPieza.getValue();
                    cmbCliente.setItems(FXCollections.observableArrayList(datos.clientes()));
                    cmbPieza.setItems(FXCollections.observableArrayList(datos.piezas()));
                    if (cliente != null) seleccionarPorId(cmbCliente, cliente.getId(), Cliente::getId);
                    if (pieza != null) seleccionarPorId(cmbPieza, pieza.getId(), Pieza::getId);
                    tablaVentas.setItems(FXCollections.observableArrayList(datos.ventas()));
                },
                error -> mensaje("No se pudieron cargar los datos: " + Tareas.mensajeCorto(error)));
    }

    private static <T> void seleccionarPorId(ComboBox<T> combo, Long id,
                                             java.util.function.Function<T, Long> idDe) {
        combo.getItems().stream().filter(item -> id.equals(idDe.apply(item)))
                .findFirst().ifPresentOrElse(combo::setValue, () -> combo.setValue(null));
    }

    /** Ejecuta una operación de escritura en segundo plano, una a la vez. */
    private <T> void ejecutarOcupado(java.util.function.Supplier<T> trabajo,
                                     java.util.function.Consumer<T> alTerminar) {
        if (ocupado) {
            mensaje("Espera a que termine la operación anterior.");
            return;
        }
        ocupado = true;
        Tareas.ejecutar(trabajo, resultado -> {
            ocupado = false;
            alTerminar.accept(resultado);
        }, error -> {
            ocupado = false;
            Tareas.mostrarError(error);
        });
    }

    /** Lee el formulario en el hilo de JavaFX; null si falta el cliente. */
    private Solicitud leerSolicitud() {
        Cliente cliente = cmbCliente.getValue();
        if (cliente == null) {
            AlertUtil.advertencia("Debe seleccionar un cliente.");
            return null;
        }
        List<SolicitudDetalleVenta> detalles = carrito.stream()
                .map(linea -> new SolicitudDetalleVenta(
                        linea.pieza().getId(), linea.cantidad(), linea.descuento()))
                .toList();
        return new Solicitud(ventaEnEdicionId, cliente.getId(), cmbMetodoPago.getValue(), detalles);
    }

    private Venta guardar(Solicitud s) {
        return s.ventaId() == null
                ? ventaService.crearEnPreparacion(s.clienteId(), s.metodoPago(), s.detalles())
                : ventaService.modificarEnPreparacion(s.ventaId(), s.clienteId(), s.metodoPago(), s.detalles());
    }

    private void cargarVenta(Venta resumen) {
        if (resumen == null) {
            return;
        }
        Long id = resumen.getId();
        Tareas.ejecutar(() -> ventaService.buscarConDetalles(id), venta -> {
            Venta seleccionada = tablaVentas.getSelectionModel().getSelectedItem();
            if (seleccionada == null || !id.equals(seleccionada.getId())) {
                return; // el usuario ya eligió otra venta
            }
            ventaEnEdicionId = venta.getId();
            ventaHistorica = venta.getEstado() != EstadoVenta.PREPARACION;
            seleccionarPorId(cmbCliente, venta.getCliente().getId(), Cliente::getId);
            cmbMetodoPago.setValue(venta.getMetodoPago());
            carrito.setAll(venta.getDetalles().stream().map(this::aLinea).toList());
            actualizarTotal();
            mensaje(venta.getEstado() == EstadoVenta.PREPARACION
                    ? "Venta en preparacion cargada para editar."
                    : "Venta historica: " + venta.getEstado() + ".");
        });
    }

    private LineaCarrito aLinea(DetalleVenta detalle) {
        return new LineaCarrito(
                detalle.getPieza(), detalle.getCantidad(), detalle.getPrecioUnitario(), detalle.getCostoUnitarioVenta(),
                detalle.getDescuentoAplicado(), detalle.getSubtotal());
    }

    private void recargarVentas() {
        Tareas.ejecutar(ventaService::listar,
                ventas -> tablaVentas.setItems(FXCollections.observableArrayList(ventas)),
                error -> mensaje("No se pudieron cargar las ventas: " + Tareas.mensajeCorto(error)));
    }

    private void mostrarDescuentoMaximo(Pieza pieza) {
        if (pieza == null) {
            lblDescuentoMaximo.setText("Descuento máximo: --");
            return;
        }
        try {
            BigDecimal maximo = CalculadoraVenta.calcularDescuentoMaximo(
                    BigDecimal.valueOf(pieza.getPrecioVenta()), BigDecimal.valueOf(pieza.getCosto()));
            lblDescuentoMaximo.setText("Descuento máximo por unidad: C$ " + maximo);
        } catch (RuntimeException e) {
            lblDescuentoMaximo.setText(e.getMessage());
        }
    }

    private void actualizarTotal() {
        BigDecimal total = CalculadoraVenta.calcularTotal(
                carrito.stream().map(LineaCarrito::subtotal).toList());
        lblTotal.setText("Total: " + com.quilate.util.Formatos.dinero(total));
        BigDecimal costo = carrito.stream().map(l -> l.costo().multiply(BigDecimal.valueOf(l.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal descuentos = carrito.stream().map(l -> l.descuento().multiply(BigDecimal.valueOf(l.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ganancia = total.subtract(costo);
        lblGanancia.setText("Ganancia bruta: " + com.quilate.util.Formatos.dinero(ganancia));
        lblMargen.setText(carrito.isEmpty() ? "Agrega piezas para calcular el margen."
                : ganancia.signum()==0 ? "Punto de equilibrio · sin ganancia bruta"
                : "Margen: " + com.quilate.service.AnalisisService.margen(ganancia,total) + " % · antes de gastos operativos");
        lblAhorro.setText("Descuentos: " + com.quilate.util.Formatos.dinero(descuentos));
    }

    private BigDecimal decimalOZero(String texto) {
        if (texto == null || texto.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(texto.trim().replace("C$", "").replace(",", "").trim());
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("El descuento debe ser un número, por ejemplo 50.00.");
        }
    }

    private void mensaje(String texto) {
        lblMensaje.setText(texto);
    }


    private record LineaCarrito(Pieza pieza, int cantidad, BigDecimal precio, BigDecimal costo,
                                BigDecimal descuento, BigDecimal subtotal) {
    }
}
