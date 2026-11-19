package com.quilate.ui;

import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;

/** Sello de Quilate: una gema facetada sobre esmeralda con un filo dorado. */
public final class MarcaQuilate extends Group {
    public MarcaQuilate() {
        this(44);
    }

    public MarcaQuilate(double pixels) {
        Color oro = Color.web("#E8CB87");
        Circle sello = new Circle(32, 32, 30.5, Color.web("#173C32"));
        sello.setStroke(oro);
        sello.setStrokeWidth(1.5);
        SVGPath gema = new SVGPath();
        gema.setContent("M12 27 L22 17 H42 L52 27 L32 48 Z M12 27 H52 M22 17 L25 27 L32 48 L39 27 L42 17 M25 27 L32 17 L39 27");
        gema.setFill(Color.TRANSPARENT);
        gema.setStroke(oro);
        gema.setStrokeWidth(1.65);
        Group figura = new Group(sello, gema);
        figura.getTransforms().add(new Scale(pixels / 64, pixels / 64));
        getChildren().add(figura);
        setMouseTransparent(true);
    }

    public static Image imagen(double pixels) {
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.TRANSPARENT);
        return new MarcaQuilate(pixels).snapshot(parametros, null);
    }
}
