package com.quilate.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;

/** Marca ficticia de Joyería Quilate: monograma Q sobre un círculo marfil. */
public final class MarcaQuilate {
    private MarcaQuilate() { }

    public static Node crear(double tamano) {
        Circle fondo = new Circle(50, 50, 50, Color.web("#E8E2DC"));
        Color tono = Color.web("#BA9A90");

        SVGPath aro = new SVGPath();
        aro.setFillRule(FillRule.EVEN_ODD);
        aro.setContent("M50 22 A24 24 0 1 0 50.01 22 Z "
                + "M50 30 A16 16 0 1 1 49.99 30 Z");
        aro.setFill(tono);

        SVGPath cola = new SVGPath();
        cola.setContent("M52 63 L56 57 L80 72 L76 78 Z");
        cola.setFill(tono);

        Group logo = new Group(fondo, aro, cola);
        logo.getTransforms().add(new Scale(tamano / 100, tamano / 100));
        Group resultado = new Group(logo);
        resultado.setAccessibleText("Joyería Quilate, monograma Q");
        return resultado;
    }

    public static Image imagen(int tamano) {
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.TRANSPARENT);
        return crear(tamano).snapshot(parametros, null);
    }
}
