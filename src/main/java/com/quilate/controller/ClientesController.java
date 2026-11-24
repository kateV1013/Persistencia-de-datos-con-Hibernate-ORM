package com.quilate.controller;

import com.quilate.model.*;
import com.quilate.service.*;
import com.quilate.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ClientesController implements com.quilate.ui.Actualizable {
    @FXML private TableView<Cliente> tabla;
    @FXML private TableView<HistorialCompra> historial;
    @FXML private TextField nombre, apellido, telefono, correo;
    @FXML private Label mensaje, lblPerfilCompras, lblPerfilMonto, lblPerfilDia, lblPerfilHora;
    private long revisionPerfil;
    private record DatosPerfil(java.util.List<HistorialCompra> historial, AnalisisService.Resumen analisis) { }
    private Cliente seleccionado;
    private void limpiarPerfil() {
        lblPerfilCompras.setText("—"); lblPerfilMonto.textProperty().unbind(); lblPerfilMonto.setText("—");
        lblPerfilDia.setText("Selecciona un cliente"); lblPerfilHora.setText("Selecciona un cliente");
    }
    private final ClienteService servicio = new ClienteService();
    @FXML private void initialize() {
        Tablas.columna(tabla, "Cliente", Cliente::getNombreCompleto);
        Tablas.columna(tabla, "Teléfono", Cliente::getTelefono);
        Tablas.columna(tabla, "Correo", Cliente::getCorreo);
        Tablas.columna(historial, "Fecha y hora", HistorialCompra::fecha);
        Tablas.columna(historial, "Pieza", HistorialCompra::pieza);
        Tablas.columna(historial, "Cantidad", HistorialCompra::cantidad);
        Tablas.columna(historial, "Total neto", HistorialCompra::total);
        tabla.getSelectionModel().selectedItemProperty().addListener((o,a,c) -> {
            if(c == null) return;
            seleccionado=c;
            nombre.setText(c.getNombre()); apellido.setText(c.getApellido());
            telefono.setText(c.getTelefono()); correo.setText(c.getCorreo());
            long revision=++revisionPerfil;
            historial.getItems().clear(); lblPerfilCompras.setText("—"); lblPerfilMonto.textProperty().unbind(); lblPerfilMonto.setText("—");
            lblPerfilDia.setText("Calculando…"); lblPerfilHora.setText("Calculando…");
            Tareas.ejecutar(() -> new DatosPerfil(new ApartadoService().historial(c.getId()), new AnalisisService().perfil(c.getId())), datos -> {
                if(revision!=revisionPerfil) return;
                historial.getItems().setAll(datos.historial());
                lblPerfilCompras.setText(Long.toString(datos.analisis().total().ventas()));
                lblPerfilMonto.textProperty().bind(com.quilate.ui.MonedaVista.importe(datos.analisis().total().ingreso()));
                lblPerfilDia.setText(datos.analisis().diaHabitual());
                lblPerfilHora.setText(datos.analisis().horaHabitual());
            }, error -> { if(revision==revisionPerfil) { limpiarPerfil(); mensaje.setText(Tareas.mensajeCorto(error)); } });
        });
        recargar();
    }
    /** Solo la tabla: el formulario y el perfil abierto se conservan. */
    @Override public void actualizarAlVolver() { recargar(); }
    @FXML private void recargar() {
        Tareas.ejecutar(servicio::listar, filas -> tabla.getItems().setAll(filas),
            e -> mensaje.setText(Tareas.mensajeCorto(e)));
    }
    @FXML private void nuevo() {
        revisionPerfil++; limpiarPerfil();
        seleccionado=null; tabla.getSelectionModel().clearSelection();
        nombre.clear(); apellido.clear(); telefono.clear(); correo.clear(); historial.getItems().clear();
    }
    @FXML private void guardar() {
        Cliente c=new Cliente(nombre.getText(), apellido.getText(), telefono.getText(), correo.getText());
        if(seleccionado!=null) { c.setId(seleccionado.getId()); c.setFechaRegistro(seleccionado.getFechaRegistro()); }
        Tareas.ejecutar(() -> servicio.guardar(c), r -> { nuevo(); recargar(); mensaje.setText("Cliente guardado."); });
    }
    @FXML private void eliminar() {
        if(seleccionado==null || !AlertUtil.confirmar("¿Eliminar este cliente sin movimientos?")) return;
        Long id=seleccionado.getId();
        Tareas.ejecutar(() -> { servicio.eliminar(id); return true; }, r -> { nuevo(); recargar(); });
    }

    // ---------- reportes ----------
    @FXML private void reporteDirectorio() {
        ReporteUtil.mostrarReporte("/reportes/clientes_directorio.jrxml", java.util.Map.of(), "Directorio de clientes");
    }
    @FXML private void reporteHistorial() {
        if (seleccionado == null) { AlertUtil.advertencia("Selecciona un cliente en la tabla para ver su historial."); return; }
        ReporteUtil.mostrarReporte("/reportes/clientes_historial.jrxml",
                java.util.Map.of("p_cliente_id", seleccionado.getId()), "Historial · " + seleccionado.getNombreCompleto());
    }
    @FXML private void reporteMejores() {
        com.quilate.ui.DialogoPeriodo.abrirReporte("/reportes/clientes_mejores.jrxml", "Mejores clientes", true);
    }
}
