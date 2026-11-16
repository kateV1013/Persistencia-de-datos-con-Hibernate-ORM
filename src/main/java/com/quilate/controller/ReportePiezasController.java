package com.quilate.controller;

import com.quilate.dto.LineaAnalisis;
import com.quilate.service.AnalisisService;
import com.quilate.service.AnalisisService.Grupo;
import com.quilate.service.AnalisisService.Resumen;
import com.quilate.ui.PresentacionTablas;
import com.quilate.util.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

/** Centro de análisis con filtros de fecha y datos históricos de ventas confirmadas. */
public class ReportePiezasController {
    @FXML private DatePicker dpDesde, dpHasta;
    @FXML private Label lblMensaje, lblIngresos, lblGanancia, lblDescuentos, lblVentas;
    @FXML private TabPane reportes;
    @FXML private Button btnGenerar;
    private long solicitud;
    @FXML private void initialize() {
        dpDesde.setValue(LocalDate.now().minusDays(29)); dpHasta.setValue(LocalDate.now());
        cargarAnalisis();
    }
    @FXML private void todoElHistorial() { dpDesde.setValue(null); dpHasta.setValue(null); onGenerar(); }
    @FXML private void onGenerar() {
        try { dpDesde.commitValue(); dpHasta.commitValue(); }
        catch (RuntimeException e) { lblMensaje.setText("Escribe fechas válidas o selecciónalas en el calendario."); return; }
        cargarAnalisis();
    }
    private void cargarAnalisis() {
        LocalDate desde=dpDesde.getValue(), hasta=dpHasta.getValue();
        if(desde!=null && hasta!=null && desde.isAfter(hasta)) { lblMensaje.setText("La fecha inicial no puede ser posterior a la final."); return; }
        long turno=++solicitud;
        btnGenerar.setDisable(true); lblMensaje.setText("Consultando ventas confirmadas…");
        Tareas.ejecutar(() -> new AnalisisService().consultar(desde,hasta), resumen -> {
            if(turno!=solicitud) return;
            mostrar(resumen); btnGenerar.setDisable(false);
            lblMensaje.setText(resumen.total().ventas()==0 ? "No hay ventas confirmadas en este período." : "Actualizado · " + resumen.total().ventas()+" ventas confirmadas");
        }, error -> { if(turno==solicitud) { btnGenerar.setDisable(false); lblMensaje.setText("No se pudo cargar: "+Tareas.mensajeCorto(error)); } });
    }
    private void mostrar(Resumen r) {
        int seleccion=Math.max(0,reportes.getSelectionModel().getSelectedIndex());
        lblIngresos.setText(Formatos.dinero(r.total().ingreso()));
        lblGanancia.setText(Formatos.dinero(r.total().ganancia()));
        lblDescuentos.setText(Formatos.dinero(r.total().descuento()));
        lblVentas.setText(Long.toString(r.total().ventas()));
        reportes.getTabs().setAll(
            grupo("Piezas", "Las 10 piezas más vendidas · unidades", r.piezas(), true),
            grupo("Días de semana", "Ventas por día de la semana", r.dias(), false),
            grupo("Horarios", "Ventas por franja de una hora", r.horas(), false),
            grupo("Mejores clientes", "Clientes por monto comprado · C$", r.clientes(), false),
            grupo("Fechas", "Ventas por fecha", r.fechas(), false), rentabilidad(r.lineas()));
        reportes.getSelectionModel().select(Math.min(seleccion,reportes.getTabs().size()-1));
    }
    private Tab grupo(String nombre, String titulo, List<Grupo> grupos, boolean unidades) {
        CategoryAxis x=new CategoryAxis(); NumberAxis y=new NumberAxis();
        boolean clientes=nombre.equals("Mejores clientes");
        BarChart<String,Number> grafico=new BarChart<>(x,y); grafico.setLegendVisible(false); grafico.setAnimated(false);
        grafico.setTitle(titulo); grafico.setMinHeight(190); grafico.setPrefHeight(240);
        x.setTickLabelRotation(nombre.equals("Horarios") ? -45 : 0);
        y.setLabel(clientes ? "Monto comprado (C$)" : unidades ? "Unidades" : "Ventas");
        XYChart.Series<String,Number> serie=new XYChart.Series<>();
        List<Grupo> visibles=(nombre.equals("Piezas")||clientes) ? grupos.stream().limit(10).toList() : grupos;
        if(nombre.equals("Fechas")) visibles=grupos.size()>31 ? grupos.subList(grupos.size()-31,grupos.size()) : grupos;
        double max=1;
        for(Grupo g:visibles) {
            Number valor=clientes ? g.ingreso() : unidades ? g.unidades() : g.ventas();
            max=Math.max(max,valor.doubleValue());
            String etiqueta=nombre.equals("Horarios") ? g.nombre().substring(0,5) : g.nombre();
            XYChart.Data<String,Number> dato=new XYChart.Data<>(etiqueta,valor);
            dato.nodeProperty().addListener((o,a,n) -> { if(n!=null) Tooltip.install(n,new Tooltip(g.nombre()+": "+valor)); });
            serie.getData().add(dato);
        }
        grafico.getData().add(serie);
        if(!clientes) { double paso=Math.max(1,Math.ceil(max/6)); y.setAutoRanging(false); y.setLowerBound(0); y.setUpperBound((Math.ceil(max/paso)+1)*paso); y.setTickUnit(paso); y.setMinorTickVisible(false); }
        TableView<Grupo> tabla=new TableView<>();
        columna(tabla,nombre.equals("Piezas")?"Pieza":clientes?"Cliente":nombre,Grupo::nombre,220);
        columna(tabla,"Ventas",Grupo::ventas,85); columna(tabla,"Unidades",Grupo::unidades,85);
        columna(tabla,"Ingreso neto",g -> Formatos.dinero(g.ingreso()),130);
        columna(tabla,"Descuentos",g -> Formatos.dinero(g.descuento()),125);
        columna(tabla,"Ganancia bruta",g -> Formatos.dinero(g.ganancia()),145);
        tabla.getItems().setAll(grupos); tabla.setMinHeight(150);
        Label nota=new Label(clientes ? "Ordenados por monto comprado; los empates se resuelven por número de compras."
            : nombre.equals("Fechas") ? "Gráfico: últimas 31 fechas con ventas. La tabla incluye todas las fechas del período."
            : "Se cuentan ventas distintas; varias piezas de una misma venta no duplican su frecuencia.");
        nota.getStyleClass().add("ayuda"); nota.setWrapText(true);
        VBox vista=new VBox(10,grafico,nota,tabla); VBox.setVgrow(tabla,Priority.ALWAYS);
        vista.getStyleClass().add("tab-contenido"); PresentacionTablas.aplicar(vista);
        return new Tab(nombre,vista);
    }
    private Tab rentabilidad(List<LineaAnalisis> lineas) {
        TableView<LineaAnalisis> tabla=new TableView<>();
        columna(tabla,"Venta",LineaAnalisis::ventaId,65);
        columna(tabla,"Fecha y hora",l -> l.fecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),150);
        columna(tabla,"Cliente",LineaAnalisis::cliente,165); columna(tabla,"Pieza",LineaAnalisis::pieza,160);
        columna(tabla,"Cantidad",LineaAnalisis::cantidad,70);
        columna(tabla,"Descuento total",l -> Formatos.dinero(l.descuentoTotal()),130);
        columna(tabla,"Ingreso neto",l -> Formatos.dinero(l.ingreso()),130);
        columna(tabla,"Costo histórico",l -> Formatos.dinero(l.costoTotal()),130);
        columna(tabla,"Ganancia bruta",l -> Formatos.dinero(l.ganancia()),130);
        columna(tabla,"Margen %",l -> AnalisisService.margen(l.ganancia(),l.ingreso())+" %",90);
        columna(tabla,"Resultado",l -> l.ganancia().signum()>0 ? "Con ganancia" : l.ganancia().signum()==0 ? "Sin ganancia" : "Pérdida",120);
        CheckBox solo=new CheckBox("Mostrar únicamente líneas con descuento");
        solo.setOnAction(e -> tabla.getItems().setAll(lineas.stream().filter(l -> !solo.isSelected() || l.descuento().signum()>0).toList()));
        tabla.getItems().setAll(lineas);
        Label nota=new Label("Ganancia bruta = ingreso después del descuento − costo histórico de las piezas. No incluye gastos operativos. Los descuentos se muestran por línea y cantidad.");
        nota.setWrapText(true); nota.getStyleClass().add("ayuda");
        VBox vista=new VBox(12,nota,solo,tabla); VBox.setVgrow(tabla,Priority.ALWAYS); vista.getStyleClass().add("tab-contenido");
        PresentacionTablas.aplicar(vista); return new Tab("Descuentos y margen",vista);
    }
    private <T> void columna(TableView<T> tabla,String titulo,Function<T,?> valor,double ancho) {
        TableColumn<T,Object> c=new TableColumn<>(titulo);
        c.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(valor.apply(f.getValue())));
        c.setPrefWidth(ancho); tabla.getColumns().add(c);
    }
}
