package com.quilate.controller;

import com.quilate.dto.LineaAnalisis;
import com.quilate.service.AnalisisService;
import com.quilate.service.AnalisisService.Grupo;
import com.quilate.service.AnalisisService.Resumen;
import com.quilate.ui.PresentacionTablas;
import com.quilate.ui.MonedaVista;
import javafx.geometry.Pos;
import javafx.scene.Node;
import java.math.BigDecimal;
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
    @FXML private StackPane selectorMoneda;
    private long solicitud;
    @FXML private void initialize() {
        selectorMoneda.getChildren().add(MonedaVista.selector());
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
        lblIngresos.textProperty().bind(MonedaVista.importe(r.total().ingreso()));
        lblGanancia.textProperty().bind(MonedaVista.importe(r.total().ganancia()));
        lblDescuentos.textProperty().bind(MonedaVista.importe(r.total().descuento()));
        lblVentas.setText(Long.toString(r.total().ventas()));
        reportes.getTabs().setAll(
            grupo("Piezas", "Las 10 piezas más vendidas · unidades", r.piezas(), true),
            grupo("Días de semana", "Ventas por día de la semana", r.dias(), false),
            grupo("Horarios", "Ventas por franja de una hora", r.horas(), false),
            grupo("Mejores clientes", "Clientes por monto comprado", r.clientes(), false),
            grupo("Fechas", "Ventas por fecha", r.fechas(), false), rentabilidad(r.lineas()));
        reportes.getSelectionModel().select(Math.min(seleccion,reportes.getTabs().size()-1));
    }
    private Tab grupo(String nombre, String titulo, List<Grupo> grupos, boolean unidades) {
        boolean clientes = nombre.equals("Mejores clientes");
        Node grafico = unidades || clientes
                ? ranking(titulo, grupos.stream().limit(10).toList(), clientes)
                : graficoVentas(nombre, titulo, grupos);
        TableView<Grupo> tabla=new TableView<>();
        columna(tabla,nombre.equals("Piezas")?"Pieza":clientes?"Cliente":nombre,Grupo::nombre,220);
        columna(tabla,"Ventas",Grupo::ventas,85); columna(tabla,"Unidades",Grupo::unidades,85);
        columnaMoneda(tabla,"Ingreso neto",Grupo::ingreso,130);
        columnaMoneda(tabla,"Descuentos",Grupo::descuento,125);
        columnaMoneda(tabla,"Ganancia bruta",Grupo::ganancia,145);
        tabla.getItems().setAll(grupos);
        tabla.setMinHeight(150);
        tabla.setPrefHeight(150);
        Label nota=new Label(clientes ? "Ordenados por monto comprado; los empates se resuelven por número de compras."
            : nombre.equals("Fechas") ? "Gráfico: últimas 31 fechas con ventas. La tabla incluye todas las fechas del período."
            : "Se cuentan ventas distintas; varias piezas de una misma venta no duplican su frecuencia.");
        nota.getStyleClass().add("ayuda"); nota.setWrapText(true);
        VBox vista=new VBox(10,grafico,nota,tabla); VBox.setVgrow(tabla,Priority.ALWAYS);
        vista.getStyleClass().add("tab-contenido"); PresentacionTablas.aplicar(vista);
        return new Tab(nombre,vista);
    }
    private Node ranking(String titulo, List<Grupo> grupos, boolean clientes) {
        Label encabezado = new Label(titulo);
        encabezado.getStyleClass().add("ranking-titulo");
        VBox lista = new VBox(2);
        lista.getStyleClass().add("ranking-lista");
        double maximo = grupos.stream().mapToDouble(g -> clientes ? g.ingreso().doubleValue() : g.unidades())
                .max().orElse(0);
        for (Grupo grupo : grupos) {
            Label nombre = new Label(grupo.nombre());
            nombre.getStyleClass().add("ranking-nombre");
            nombre.setMinWidth(145);
            nombre.prefWidthProperty().bind(lista.widthProperty().multiply(0.28));
            nombre.setTooltip(new Tooltip(grupo.nombre()));
            double dato = clientes ? grupo.ingreso().doubleValue() : grupo.unidades();
            double proporcion = maximo > 0 ? Math.max(0, dato / maximo) : 0;
            Region relleno = new Region();
            relleno.getStyleClass().add("ranking-relleno");
            relleno.setMaxHeight(10);
            StackPane pista = new StackPane(relleno);
            pista.setAlignment(Pos.CENTER_LEFT);
            pista.setMinSize(28, 10);
            pista.setMaxHeight(10);
            pista.getStyleClass().add("ranking-pista");
            relleno.maxWidthProperty().bind(pista.widthProperty().multiply(proporcion));
            HBox.setHgrow(pista, Priority.ALWAYS);

            Label valor = new Label();
            valor.getStyleClass().add("ranking-valor");
            valor.setMinWidth(clientes ? 120 : 32);
            valor.setAlignment(Pos.CENTER_RIGHT);
            if (clientes) valor.textProperty().bind(MonedaVista.importe(grupo.ingreso()));
            else valor.setText(Long.toString(grupo.unidades()));
            HBox fila = new HBox(12, nombre, pista, valor);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setMinHeight(20);
            lista.getChildren().add(fila);
        }
        VBox panel = new VBox(8, encabezado);
        panel.getStyleClass().add("ranking-panel");
        if (grupos.isEmpty()) {
            Label vacio = new Label("No hay datos para graficar en este período.");
            vacio.getStyleClass().add("ayuda");
            panel.getChildren().add(vacio);
        } else {
            ScrollPane scroll = new ScrollPane(lista);
            scroll.setFitToWidth(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setPrefViewportHeight(grupos.size() * 22 + 5);
            scroll.setMinHeight(84);
            VBox.setVgrow(scroll, Priority.ALWAYS);
            panel.getChildren().add(scroll);
        }
        return panel;
    }

    private Node graficoVentas(String nombre, String titulo, List<Grupo> grupos) {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> grafico = new BarChart<>(x, y);
        grafico.setLegendVisible(false);
        grafico.setAnimated(false);
        grafico.setTitle(titulo);
        grafico.setMinHeight(190);
        grafico.setPrefHeight(240);
        x.setTickLabelRotation(nombre.equals("Horarios") || nombre.equals("Fechas") ? -45 : 0);
        y.setLabel("Ventas");
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        List<Grupo> visibles = nombre.equals("Fechas") && grupos.size() > 31
                ? grupos.subList(grupos.size() - 31, grupos.size()) : grupos;
        double maximo = 1;
        for (Grupo grupo : visibles) {
            maximo = Math.max(maximo, grupo.ventas());
            String etiqueta = nombre.equals("Horarios") ? grupo.nombre().substring(0, 5) : grupo.nombre();
            XYChart.Data<String, Number> punto = new XYChart.Data<>(etiqueta, grupo.ventas());
            punto.nodeProperty().addListener((o, anterior, nodo) -> {
                if (nodo != null) Tooltip.install(nodo, new Tooltip(grupo.nombre() + ": " + grupo.ventas()));
            });
            serie.getData().add(punto);
        }
        grafico.getData().add(serie);
        double paso = Math.max(1, Math.ceil(maximo / 6));
        y.setAutoRanging(false);
        y.setLowerBound(0);
        y.setUpperBound((Math.ceil(maximo / paso) + 1) * paso);
        y.setTickUnit(paso);
        y.setMinorTickVisible(false);
        return grafico;
    }

    private Tab rentabilidad(List<LineaAnalisis> lineas) {
        TableView<LineaAnalisis> tabla=new TableView<>();
        columna(tabla,"Venta",LineaAnalisis::ventaId,65);
        columna(tabla,"Fecha y hora",l -> l.fecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),150);
        columna(tabla,"Cliente",LineaAnalisis::cliente,165); columna(tabla,"Pieza",LineaAnalisis::pieza,160);
        columna(tabla,"Cantidad",LineaAnalisis::cantidad,70);
        columnaMoneda(tabla,"Descuento total",LineaAnalisis::descuentoTotal,130);
        columnaMoneda(tabla,"Ingreso neto",LineaAnalisis::ingreso,130);
        columnaMoneda(tabla,"Costo histórico",LineaAnalisis::costoTotal,130);
        columnaMoneda(tabla,"Ganancia bruta",LineaAnalisis::ganancia,130);
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

    private <T> void columnaMoneda(TableView<T> tabla, String titulo, Function<T, BigDecimal> valor, double ancho) {
        TableColumn<T, BigDecimal> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(valor.apply(f.getValue())));
        columna.setPrefWidth(ancho);
        columna.getStyleClass().add("importe");
        tabla.getColumns().add(columna);
    }

    // ---------- reportes: usan el período elegido arriba (vacío = todo el historial) ----------
    @FXML private void reporteResumen() { reporte("analisis_resumen", "Resumen de ventas"); }
    @FXML private void reporteMasVendidas() { reporte("analisis_piezas_mas_vendidas", "Piezas más vendidas"); }
    @FXML private void reporteRentabilidad() { reporte("analisis_rentabilidad", "Rentabilidad por pieza"); }
    private void reporte(String archivo, String titulo) {
        try { dpDesde.commitValue(); dpHasta.commitValue(); }
        catch (RuntimeException e) { lblMensaje.setText("Escribe fechas válidas o selecciónalas en el calendario."); return; }
        try { ReporteUtil.mostrarReporte("/reportes/" + archivo + ".jrxml", ReporteUtil.rangoFechaHora(dpDesde.getValue(), dpHasta.getValue()), titulo); }
        catch (RuntimeException e) { Tareas.mostrarError(e); }
    }
}
