package com.quilate.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public final class AlertUtil {

    private AlertUtil() {
    }

    public static void info(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "Informacion", mensaje);
    }

    public static void advertencia(String mensaje) {
        mostrar(Alert.AlertType.WARNING, "Atencion", mensaje);
    }

    public static void error(String mensaje) {
        mostrar(Alert.AlertType.ERROR, "Error", mensaje);
    }

    public static void error(Throwable e) {
        Throwable causa = e;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        String detalle = causa.getMessage() != null ? causa.getMessage() : causa.getClass().getSimpleName();
        mostrar(Alert.AlertType.ERROR, "Error", detalle);
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

    /** Los dialogos usan la misma paleta que el resto de la aplicacion. */
    private static void aplicarEstilos(Alert alert) {
        var css = AlertUtil.class.getResource("/css/styles.css");
        if (css != null) {
            alert.getDialogPane().getStylesheets().add(css.toExternalForm());
        }
    }
}
