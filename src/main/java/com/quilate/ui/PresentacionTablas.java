package com.quilate.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.math.BigDecimal;
import java.util.Locale;

/** Presentación común de tablas; conserva los datos y las reglas de negocio. */
public final class PresentacionTablas {
    private PresentacionTablas() { }
    public static void aplicar(Node nodo) {
        if (nodo instanceof TableView<?> tabla) {
            Label titulo = new Label("Aún no hay registros");
            titulo.getStyleClass().add("empty-title");
            String detalle = switch (tabla.getId() == null ? "" : tabla.getId()) {
                case "tablaDetalles" -> "Selecciona una pieza y pulsa Agregar pieza.";
                case "historial" -> "Selecciona un cliente con compras confirmadas.";
                default -> "Los registros aparecerán aquí al cargar los datos.";
            };
            Label ayuda = new Label(detalle);
            ayuda.setWrapText(true);
            ayuda.getStyleClass().add("empty-detail");
            VBox vacio = new VBox(10, Iconos.crear(Iconos.DIAMANTE, 30, Color.web("#B89362")), titulo, ayuda);
            vacio.setAlignment(Pos.CENTER);
            vacio.getStyleClass().add("empty-state");
            tabla.setPlaceholder(vacio);
            for (TableColumn<?, ?> columna : tabla.getColumns()) {
                String tituloColumna = columna.getText().toLowerCase(Locale.ROOT);
                double minimo = switch (tituloColumna) {
                    case "#", "id", "n.º" -> 42;
                    case "cant.", "stock", "existencias", "cantidad", "unidades" -> 65;
                    case "fecha", "fecha y hora" -> 138;
                    case "pieza", "cliente", "servicio" -> 145;
                    default -> 115;
                };
                columna.setMinWidth(minimo);
                columna.setPrefWidth(Math.max(minimo, columna.getPrefWidth()));
                configurar(columna);
                registrarMoneda(tabla, columna);
            }
            // En ventanas pequeñas se permite desplazamiento horizontal; ninguna columna se reduce a cero.
            tabla.widthProperty().addListener((o, a, b) -> ajustarAncho(tabla));
            ajustarAncho(tabla);
        }
        if (nodo instanceof ScrollPane scroll) aplicar(scroll.getContent());
        else if (nodo instanceof SplitPane split) split.getItems().forEach(PresentacionTablas::aplicar);
        else if (nodo instanceof TitledPane titulo) aplicar(titulo.getContent());
        else if (nodo instanceof Parent padre) padre.getChildrenUnmodifiable().forEach(PresentacionTablas::aplicar);
    }
    private static void ajustarAncho(TableView<?> tabla) {
        double minimo = tabla.getColumns().stream().filter(TableColumn::isVisible)
                .mapToDouble(TableColumn::getMinWidth).sum() + 20;
        tabla.setColumnResizePolicy(tabla.getWidth() >= minimo
                ? TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
                : TableView.UNCONSTRAINED_RESIZE_POLICY);
    }
    private static boolean esImporte(TableColumn<?, ?> columna) {
        String nombre = columna.getText().toLowerCase(Locale.ROOT);
        return columna.getStyleClass().contains("importe") || nombre.contains("precio")
                || nombre.contains("costo") || nombre.contains("total") || nombre.contains("descuento");
    }

    private static void registrarMoneda(TableView<?> tabla, TableColumn<?, ?> columna) {
        if (esImporte(columna) && tabla.getProperties().putIfAbsent("quilate-moneda", true) == null) {
            MonedaVista.observar(tabla, tabla::refresh);
        }
    }

    private static <S,T> void configurar(TableColumn<S,T> columna) {
        String nombre = columna.getText().toLowerCase(Locale.ROOT);
        boolean dinero = esImporte(columna);
        boolean estado = nombre.equals("estado");
        boolean fecha = nombre.contains("fecha");
        if (dinero) { columna.setMinWidth(110); columna.setPrefWidth(Math.max(110, columna.getPrefWidth())); }
        if (estado) { columna.setMinWidth(110); columna.setPrefWidth(115); }
        if (!dinero && !estado && !fecha) return;
        columna.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(T valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(null); setGraphic(null); setTooltip(null);
                if (vacia || valor == null) return;
                if (dinero && valor instanceof Number numero) {
                    setAlignment(Pos.CENTER_RIGHT);
                    setText(MonedaVista.formatear(new BigDecimal(numero.toString())));
                } else if (estado) {
                    String codigo = valor.toString().toUpperCase(Locale.ROOT);
                    Label insignia = new Label(textoEstado(valor));
                    insignia.getStyleClass().add("estado-chip");
                    if (codigo.contains("CANCEL") || codigo.contains("ANULA")) insignia.getStyleClass().add("estado-cancelado");
                    else if (codigo.contains("PEND") || codigo.contains("PREPAR")) insignia.getStyleClass().add("estado-pendiente");
                    setGraphic(insignia);
                } else if (fecha && valor instanceof java.time.LocalDateTime tiempo) {
                    setText(tiempo.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                } else {
                    if (dinero) setAlignment(Pos.CENTER_RIGHT);
                    setText(valor.toString());
                }
                setTooltip(new Tooltip(getText() == null ? valor.toString() : getText()));
            }
        });
    }

    /** Texto de la insignia de estado: "PREPARACION" → "En preparación", "EN_PROCESO" → "En proceso". */
    static String textoEstado(Object valor) {
        String codigo = valor.toString().toUpperCase(Locale.ROOT);
        if (codigo.equals("PREPARACION")) return "En preparación";
        return codigo.substring(0, 1) + codigo.substring(1).toLowerCase(Locale.ROOT).replace('_', ' ');
    }
}
