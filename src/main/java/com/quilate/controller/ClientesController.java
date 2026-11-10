package com.quilate.controller;

import com.quilate.model.*;
import com.quilate.service.*;
import com.quilate.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ClientesController {
    @FXML private TableView<Cliente> tabla;
    @FXML private TableView<HistorialCompra> historial;
    @FXML private TextField nombre, apellido, telefono, correo;
    @FXML private Label mensaje;
    private Cliente seleccionado;
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
            Tareas.ejecutar(() -> new ApartadoService().historial(c.getId()), filas -> historial.getItems().setAll(filas));
        });
        recargar();
    }
    @FXML private void recargar() {
        Tareas.ejecutar(servicio::listar, filas -> tabla.getItems().setAll(filas),
            e -> mensaje.setText(Tareas.mensajeCorto(e)));
    }
    @FXML private void nuevo() {
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
}
