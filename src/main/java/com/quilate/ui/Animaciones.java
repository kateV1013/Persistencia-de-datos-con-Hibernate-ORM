package com.quilate.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/** Animaciones cortas y discretas para dar sensacion de fluidez. */
public final class Animaciones {

    private static final Duration DURACION_ENTRADA = Duration.millis(320);
    private static final Duration DURACION_HOVER = Duration.millis(140);

    private Animaciones() {
    }

    /** Aparece desvaneciendose y subiendo unos pixeles. */
    public static void entrada(Node nodo, Duration retraso) {
        nodo.setOpacity(0);
        nodo.setTranslateY(18);

        FadeTransition fade = new FadeTransition(DURACION_ENTRADA, nodo);
        fade.setToValue(1);
        TranslateTransition subir = new TranslateTransition(DURACION_ENTRADA, nodo);
        subir.setToY(0);
        subir.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition animacion = new ParallelTransition(fade, subir);
        animacion.setDelay(retraso);
        animacion.play();
    }

    /** Crece ligeramente cuando el mouse pasa por encima. */
    public static void escalarAlPasar(Node nodo, double escala) {
        ScaleTransition crecer = new ScaleTransition(DURACION_HOVER, nodo);
        crecer.setToX(escala);
        crecer.setToY(escala);
        ScaleTransition volver = new ScaleTransition(DURACION_HOVER, nodo);
        volver.setToX(1);
        volver.setToY(1);

        nodo.setOnMouseEntered(e -> {
            volver.stop();
            crecer.playFromStart();
        });
        nodo.setOnMouseExited(e -> {
            crecer.stop();
            volver.playFromStart();
        });
    }
}
