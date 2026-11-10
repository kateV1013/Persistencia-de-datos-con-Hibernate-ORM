package com.quilate.controller;

import com.quilate.model.*;
import com.quilate.dao.*;
import com.quilate.service.*;
import com.quilate.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ApartadosController {
    @FXML private TableView<Apartado> tabla;
    @FXML private ComboBox<Cliente> cliente;
    @FXML private ComboBox<Pieza> pieza;
    @FXML private TextField cantidad;
    @FXML private Label mensaje;
    private Apartado seleccionado;
    private final ApartadoService servicio=new ApartadoService();
    @FXML private void initialize() {
        Tablas.columna(tabla, "Cliente", a -> a.getCliente().getNombreCompleto());
        Tablas.columna(tabla, "Pieza", a -> a.getPieza().getNombre());
        Tablas.columna(tabla, "Cantidad", Apartado::getCantidad);
        Tablas.columna(tabla, "Fecha", Apartado::getFecha);
        Tablas.columna(tabla, "Estado", Apartado::getEstado);
        tabla.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> {
            if(b==null) return; seleccionado=b;
            cliente.setValue(b.getCliente()); pieza.setValue(b.getPieza());
            cantidad.setText(Integer.toString(b.getCantidad()));
            cliente.setDisable(true); pieza.setDisable(true);
        });
        recargar();
    }
    @FXML private void recargar() {
        Tareas.ejecutar(servicio::listar, r -> tabla.getItems().setAll(r), e -> mensaje.setText(Tareas.mensajeCorto(e)));
        Tareas.ejecutar(() -> new ClienteDAO().listar(), r -> cliente.getItems().setAll(r), e -> mensaje.setText(Tareas.mensajeCorto(e)));
        Tareas.ejecutar(() -> new PiezaDAO().listarTodas(), r -> pieza.getItems().setAll(r), e -> mensaje.setText(Tareas.mensajeCorto(e)));
    }
    @FXML private void nuevo() {
        seleccionado=null; tabla.getSelectionModel().clearSelection(); cliente.setValue(null); pieza.setValue(null);
        cliente.setDisable(false); pieza.setDisable(false); cantidad.setText("1");
    }
    @FXML private void guardar() {
        try {
            int n=Integer.parseInt(cantidad.getText().trim());
            Long id=seleccionado==null?null:seleccionado.getId();
            Long c=cliente.getValue()==null?null:cliente.getValue().getId();
            Long p=pieza.getValue()==null?null:pieza.getValue().getId();
            Tareas.ejecutar(() -> { if(id==null) servicio.apartar(c,p,n); else servicio.modificarCantidad(id,n); return true; },
                r -> { nuevo(); recargar(); mensaje.setText("Reserva guardada e inventario actualizado."); });
        } catch(NumberFormatException e) { AlertUtil.advertencia("Cantidad: ingresa un entero positivo."); }
    }
    @FXML private void cancelar() {
        if(seleccionado==null || !AlertUtil.confirmar("¿Cancelar y devolver las existencias?")) return;
        Long id=seleccionado.getId();
        Tareas.ejecutar(() -> { servicio.cancelar(id); return true; }, r -> { nuevo(); recargar(); });
    }
    @FXML private void vender() {
        if(seleccionado==null || !AlertUtil.confirmar("¿Confirmar la venta de esta reserva con pago en efectivo?")) return;
        Long id=seleccionado.getId();
        Tareas.ejecutar(() -> new VentaService().convertirApartado(id),
            v -> { nuevo(); recargar(); mensaje.setText("Venta #"+v.getId()+" confirmada sin descontar stock nuevamente."); });
    }
}
