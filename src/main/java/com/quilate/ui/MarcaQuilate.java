package com.quilate.ui;

import javafx.beans.NamedArg;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.transform.Scale;

/** Monograma de Quilate sobre un sello dorado facetado. */
public final class MarcaQuilate extends Group {
    public MarcaQuilate() {
        this(44);
    }

    public MarcaQuilate(@NamedArg(value = "pixels", defaultValue = "44") double pixels) {
        SVGPath sello = trazo("M9 20 L23 9 Q26 7 31 7 L49 6 Q53 6 56 9 L61 14 "
                + "Q62 16 61 20 L56 41 Q55 45 51 48 L38 57 Q35 59 31 58 "
                + "L14 56 Q11 56 8 52 L3 45 Q1 42 3 37 Z", "#916332", 2.2);
        sello.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#BF8D51")), new Stop(.45, Color.web("#FFF0D4")),
                new Stop(1, Color.web("#D8AC71"))));
        SVGPath bisel = trazo("M12 22 L26 11 L49 10 L57 17 L52 41 L35 54 L15 51 L7 42 Z",
                "#B28855", 1.05);
        SVGPath monograma = trazo("M24 42 C17 36 35 12 43 15 C54 18 33 49 23 41 "
                + "M17 45 C26 39 31 54 44 45", "#81522D", 1.9);
        Group figura = new Group(sello, bisel, monograma);
        figura.getTransforms().add(new Scale(pixels / 64, pixels / 64));
        getChildren().add(figura);
        setMouseTransparent(true);
    }

    private static SVGPath trazo(String contenido, String color, double grosor) {
        SVGPath figura = new SVGPath();
        figura.setContent(contenido);
        figura.setFill(Color.TRANSPARENT);
        figura.setStroke(Color.web(color));
        figura.setStrokeWidth(grosor);
        return figura;
    }

    public static Image imagen(double pixels) {
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.TRANSPARENT);
        return new MarcaQuilate(pixels).snapshot(parametros, null);
    }
}
