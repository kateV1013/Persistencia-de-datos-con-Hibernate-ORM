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

    public static final String INVENTARIO = "M20 2H4c-1 0-2 .9-2 2v3.01c0 .72.43 1.34 1 1.69V20c0 1.1 1.1 2 2 2h14c.9 0 2-.9 2-2V8.7c.57-.35 1-.97 1-1.69V4c0-1.1-1-2-2-2zm-5 12H9v-2h6v2zm5-7H4V4l16-.02V7z";
    public static final String CLIENTES = "M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z";
    public static final String APARTADOS = "M17 3H7c-1.1 0-1.99.9-1.99 2L5 21l7-3 7 3V5c0-1.1-.9-2-2-2z";
    public static final String VENTAS = "M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49c.08-.14.12-.31.12-.48 0-.55-.45-1-1-1H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z";
    public static final String PEDIDOS = "M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9-2-2-5-2.4-7.4-1.3L9 6 6 9 1.6 4.7C.4 7.1.9 10.1 2.9 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.5-.4.5-1.1.1-1.4z";
    public static final String SERVICIOS = "M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z";
    public static final String REPORTE = "M5 9.2h3V19H5zM10.6 5h2.8v14h-2.8zm5.6 8H19v6h-2.8z";
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

    /** Icono blanco dentro de un circulo con el color de acento (definido por CSS: -q-acento). */
    public static StackPane enCirculo(String trazo, double diametro) {
        StackPane circulo = new StackPane(crear(trazo, diametro * 0.5, Color.WHITE));
        circulo.getStyleClass().add("icono-circulo");
        circulo.setMinSize(diametro, diametro);
        circulo.setPrefSize(diametro, diametro);
        circulo.setMaxSize(diametro, diametro);
        return circulo;
    }

    /** Icono de la aplicacion para la barra de tareas: diamante dorado sobre azul medianoche. */
    public static Image imagenAplicacion(int tamano) {
        Rectangle fondo = new Rectangle(tamano, tamano, Color.web("#1B2436"));
        fondo.setArcWidth(tamano * 0.4);
        fondo.setArcHeight(tamano * 0.4);
        StackPane icono = new StackPane(fondo, crear(DIAMANTE, tamano * 0.7, Color.web("#D4AF37")));
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.TRANSPARENT);
        return icono.snapshot(parametros, null);
    }
}
