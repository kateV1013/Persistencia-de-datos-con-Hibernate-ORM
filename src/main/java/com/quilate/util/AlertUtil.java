package com.quilate.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public final class AlertUtil {

    private AlertUtil() {
    }

    public static void info(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "Información", mensaje);
    }

    public static void advertencia(String mensaje) {
        mostrar(Alert.AlertType.WARNING, "Atención", mensaje);
    }

    public static void error(String mensaje) {
        mostrar(Alert.AlertType.ERROR, "Error", mensaje);
    }

    public static void error(Throwable e) {
        mostrar(Alert.AlertType.ERROR, "Error", Tareas.mensajeCorto(e));
    }

    public static boolean confirmar(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmar");
        alert.setHeaderText(null);
        aplicarEstilos(alert);
        Optional<ButtonType> r = alert.showAndWait();
        return r.isPresent() && r.get() == ButtonType.YES;
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo, mensaje, ButtonType.OK);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        aplicarEstilos(alert);
        alert.showAndWait();
    }

    /** Ventana dueña, paleta de la app y alto según el texto (ver {@link com.quilate.ui.Dialogos}). */
    private static void aplicarEstilos(Alert alert) {
        com.quilate.ui.Dialogos.preparar(alert);
    }
}
