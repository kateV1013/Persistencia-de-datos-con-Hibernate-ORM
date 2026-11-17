package com.quilate.ui;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import java.util.ArrayList;
import java.util.Locale;

/** Decoración compartida de formularios y acciones sin duplicar lógica de negocio. */
public final class FormulariosVisuales {
    private FormulariosVisuales() { }
    private static final String GUARDAR="M5 3h12l4 4v14H3V3zm2 1v6h10V4zM7 14v6h10v-6z";
    private static final String MAS="M11 4h2v7h7v2h-7v7h-2v-7H4v-2h7z";
    private static final String BORRAR="M8 2h8v2h5v2H3V4h5zm-3 6h14l-1 14H6zm4 2v9h2v-9zm4 0v9h2v-9z";
    private static final String ACTUALIZAR="M18 6A8 8 0 1 0 20 16h-3a5 5 0 1 1-1-8l-3 3h9V2z";
    private static final String CHECK="M2 12l3-3 5 5L20 4l3 3L10 20z";
    private static final String BUSCAR="M10 2a8 8 0 1 0 5 14l6 6 2-2-6-6A8 8 0 0 0 10 2zm0 3a5 5 0 1 1 0 10 5 5 0 0 1 0-10z";
    public static void aplicar(Node n) {
        if(n==null) return;
        if(n instanceof VBox box && box.getStyleClass().contains("formulario") && !box.getProperties().containsKey("quilate-form")) {
            box.getProperties().put("quilate-form",true);
            if(!box.getChildren().isEmpty() && box.getChildren().getFirst() instanceof Label titulo && titulo.getStyleClass().contains("titulo-seccion")) {
                box.getChildren().remove(titulo);
                Label detalle=new Label("DATOS DEL REGISTRO");detalle.getStyleClass().add("form-kicker");
                VBox textos=new VBox(3,detalle,titulo);
                StackPane emblema=Iconos.enCirculo(Iconos.DIAMANTE,36);emblema.getStyleClass().add("form-emblema");
                HBox cabecera=new HBox(11,emblema,textos);cabecera.setAlignment(Pos.CENTER_LEFT);cabecera.getStyleClass().add("form-head");
                box.getChildren().add(0,cabecera);
            }
        }
        if(n instanceof DatePicker fecha && !fecha.getProperties().containsKey("quilate-fecha")) {
            fecha.getProperties().put("quilate-fecha",true);
            var formato=java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT);
            fecha.setConverter(new javafx.util.StringConverter<java.time.LocalDate>() {
                @Override public String toString(java.time.LocalDate valor) { return valor==null?"":formato.format(valor); }
                @Override public java.time.LocalDate fromString(String valor) { return valor==null||valor.isBlank()?null:java.time.LocalDate.parse(valor.trim(),formato); }
            });
            fecha.setPromptText("dd/mm/aaaa");
        }
        if(n instanceof Button b && b.getGraphic()==null && !b.getStyleClass().contains("nav-inicio")) {
            String t=b.getText()==null?"":b.getText().toLowerCase(Locale.ROOT);
            String icono=t.contains("guardar")?GUARDAR:t.contains("eliminar")||t.contains("anular")||t.contains("cancelar")||t.contains("quitar")?BORRAR:
                t.contains("nuev")||t.contains("agregar")||t.contains("añadir")||t.contains("crear")?MAS:
                t.contains("actualizar")||t.contains("historial")?ACTUALIZAR:t.contains("confirmar")||t.contains("convertir")?CHECK:
                t.contains("buscar")?BUSCAR:t.contains("diseñar")?Iconos.DIAMANTE:null;
            if(icono!=null) {
                boolean claro=b.getStyleClass().contains("secundario")||b.getStyleClass().contains("peligro");
                b.setGraphic(Iconos.crear(icono,13,Color.web(b.getStyleClass().contains("peligro")?"#A43F42":claro?"#75594E":"#FFFFFF")));
                b.setGraphicTextGap(7);
            }
        }
        if(n instanceof ScrollPane sp) aplicar(sp.getContent());
        else if(n instanceof TitledPane tp) aplicar(tp.getContent());
        else if(n instanceof SplitPane sp) sp.getItems().forEach(FormulariosVisuales::aplicar);
        else if(n instanceof TabPane tp) tp.getTabs().forEach(t -> aplicar(t.getContent()));
        else if(n instanceof Parent padre) new ArrayList<>(padre.getChildrenUnmodifiable()).forEach(FormulariosVisuales::aplicar);
    }
}
