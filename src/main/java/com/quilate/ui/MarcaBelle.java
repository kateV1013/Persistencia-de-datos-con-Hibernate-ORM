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

/** Adaptación vectorial del monograma JB de la referencia proporcionada. */
public final class MarcaBelle {
    private MarcaBelle() { }

    public static Node crear(double tamano) {
        Circle fondo = new Circle(50, 50, 50, Color.web("#E8E2DC"));
        SVGPath monograma = new SVGPath();
        monograma.setFillRule(FillRule.EVEN_ODD);
        monograma.setContent("M33 20 H53 C72 20 73 37 59 41 "
                + "C79 44 76 67 54 67 H48 V70 C48 83 39 86 32 83 "
                + "C24 81 25 74 30 74 C36 74 35 82 39 81 "
                + "C41 80 41 75 41 69 V27 C41 22 39 21 33 21 Z "
                + "M48 22 V40 H51 C62 40 63 22 52 22 Z "
                + "M48 43 V65 H53 C68 65 67 43 53 43 Z");
        monograma.setFill(Color.web("#BA9A90"));
        Group logo = new Group(fondo, monograma);
        logo.getTransforms().add(new Scale(tamano / 100, tamano / 100));
        Group resultado = new Group(logo);
        resultado.setAccessibleText("Joyeria Belle, monograma JB");
        return resultado;
    }

    public static Image imagen(int tamano) {
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.TRANSPARENT);
        return crear(tamano).snapshot(parametros, null);
    }
}
