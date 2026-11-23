package com.quilate.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

/**
 * Iconos vectoriales (trazos SVG de 24x24) usados en el menu principal y en
 * el encabezado de cada ventana. Al ser vectoriales se ven nitidos a cualquier tamano.
 */
public final class Iconos {

    public static final String INVENTARIO = "M3 8h18v12H3z M2 4h20v4H2z M9 12h6";
    public static final String CLIENTES = "M10 8a3.5 3.5 0 1 1-7 0 3.5 3.5 0 0 1 7 0z "
            + "M20 7a3.5 3.5 0 1 1-7 0 3.5 3.5 0 0 1 7 0z M1 20v-1a5.5 5.5 0 0 1 11 0v1z "
            + "M12 20h11v-2a5.5 5.5 0 0 0-10-3.2";
    public static final String APARTADOS = "M6 3h12v18l-6-4.5L6 21z";
    public static final String VENTAS = "M1 4h3l3 12h13l3-9H5 M10 20a1.4 1.4 0 1 1-2.8 0 1.4 1.4 0 0 1 2.8 0z "
            + "M20 20a1.4 1.4 0 1 1-2.8 0 1.4 1.4 0 0 1 2.8 0z";
    public static final String PEDIDOS = "M10 11 2.5 18.5a1.8 1.8 0 0 0 2.5 2.5l8-8 "
            + "M7 7l3 1 3 3-.5 2.5 2 2 2.5-2.5 3 3 3-3-3-3L18 10l-5-5c-2-2-5-2-8 0z";
    public static final String SERVICIOS = "M5 4h14l4 6L12 21 1 10z M1 10h22 M5 4l3 6 4 11 4-11 3-6 M8 10l4-6 4 6";
    public static final String REPORTE = "M3 13h3v8H3z M10 8h3v13h-3z M17 3h3v18h-3z";
    public static final String FLECHA = "M3 12h17 M14 6l6 6-6 6";
    public static final String BUSCAR = "M16 10a6 6 0 1 1-12 0 6 6 0 0 1 12 0z M14.5 14.5 21 21";
    /** Diamante del logotipo. */
    public static final String DIAMANTE = "M6 3h12l4 6-10 12L2 9zM4.6 9h4.3L12 18.2zM10.9 9h2.2L12 12.4zM15.1 9h4.3L12 18.2zM7.9 5 5.3 8.2h3.9zM9.6 5l1.4 3.2h2l1.4-3.2zM16.1 5l-1.3 3.2h3.9z";

    private Iconos() {
    }

    /** Icono escalado a {@code tamano} pixeles. Se envuelve en Group para que el layout respete la escala. */
    public static Node crear(String trazo, double tamano, Paint relleno) {
        SVGPath svg = new SVGPath();
        svg.setContent(trazo);
        svg.setFill(relleno);
        double escala = tamano / 24.0;
        svg.setScaleX(escala);
        svg.setScaleY(escala);
        return new Group(svg);
    }

    public static Node trazo(String contenido, double tamano) {
        SVGPath svg = new SVGPath();
        svg.setContent(contenido);
        svg.getStyleClass().add("icono-trazo");
        double escala = tamano / 24.0;
        svg.setScaleX(escala);
        svg.setScaleY(escala);
        return new Group(svg);
    }

    /** Icono blanco dentro de un circulo con el color de acento (definido por CSS: -q-acento). */
    public static StackPane enCirculo(String trazo, double diametro) {
        StackPane circulo = new StackPane(crear(trazo, diametro * 0.5, Color.WHITE));
        circulo.getStyleClass().add("icono-circulo");
        circulo.setMinSize(diametro, diametro);
        circulo.setPrefSize(diametro, diametro);
        circulo.setMaxSize(diametro, diametro);
        return circulo;
    }

    /** Icono de la aplicacion para la barra de tareas: diamante dorado sobre burdeos. */
    public static Image imagenAplicacion(int tamano) {
        Rectangle fondo = new Rectangle(tamano, tamano, Color.web("#5D111B"));
        fondo.setArcWidth(tamano * 0.4);
        fondo.setArcHeight(tamano * 0.4);
        StackPane icono = new StackPane(fondo, crear(DIAMANTE, tamano * 0.7, Color.web("#D4B778")));
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.TRANSPARENT);
        return icono.snapshot(parametros, null);
    }
}
