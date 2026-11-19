package com.quilate.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

/** Ilustración vectorial de la marca; no necesita imágenes ni conexión. */
public final class DiamanteEditorial {
    private DiamanteEditorial() { }
    public static Node crear() {
        Group grupo = new Group();
        for (int radio : new int[]{68, 88}) {
            Circle aro = new Circle(100, 85, radio);
            aro.setFill(Color.TRANSPARENT);
            aro.setStroke(Color.web("#BA9A90", .18));
            grupo.getChildren().add(aro);
        }
        SVGPath gema = new SVGPath();
        gema.setContent("M45 52 L70 27 L130 27 L155 52 L100 143 Z M45 52 L155 52 M70 27 L78 52 L100 143 L122 52 L130 27 M78 52 L100 27 L122 52");
        gema.setFill(Color.web("#BA9A90", .07));
        gema.setStroke(Color.web("#BA9A90"));
        gema.setStrokeWidth(1.6);
        grupo.getChildren().add(gema);
        for (double[] punto : new double[][]{{26,41},{165,122},{142,13}}) {
            Circle brillo = new Circle(punto[0], punto[1], 2.5, Color.web("#BA9A90"));
            grupo.getChildren().add(brillo);
        }
        return grupo;
    }
}
