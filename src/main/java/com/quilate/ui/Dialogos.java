package com.quilate.ui;

import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

/**
 * Da a todos los diálogos el mismo comportamiento en Windows y macOS.
 *
 * <ul>
 *   <li><b>Ventana dueña:</b> el diálogo se abre encima de la ventana que lo pidió. Sin dueña,
 *       macOS lo manda a su propio espacio de pantalla completa (fondo negro) y en Windows
 *       puede quedar detrás de la ventana activa.</li>
 *   <li><b>Alto según el contenido:</b> en Windows los mensajes largos se cortaban con «…».</li>
 *   <li><b>Misma apariencia:</b> hoja de estilos e ícono de Quilate; «Cancelar» y «No» como
 *       botones secundarios para distinguirlos de la acción principal.</li>
 * </ul>
 */
public final class Dialogos {

    private Dialogos() {
    }

    /** Prepara el diálogo antes de mostrarlo. Devuelve el mismo diálogo para encadenar. */
    public static <D extends Dialog<?>> D preparar(D dialogo) {
        if (dialogo.getOwner() == null) {
            Window duena = ventanaActiva();
            if (duena != null) {
                dialogo.initOwner(duena);
            }
        }
        DialogPane panel = dialogo.getDialogPane();
        String hoja = Ventanas.hojaEstilos();
        if (!panel.getStylesheets().contains(hoja)) {
            panel.getStylesheets().add(hoja);
        }
        panel.setMinHeight(Region.USE_PREF_SIZE);
        panel.getStyleClass().add("dialogo-quilate");
        // addEventHandler y no setOnShowing: no reemplaza otro manejador que el diálogo ya tenga.
        dialogo.addEventHandler(DialogEvent.DIALOG_SHOWING, e -> {
            for (ButtonType tipo : panel.getButtonTypes()) {
                Node boton = panel.lookupButton(tipo);
                if (boton != null && esSecundario(tipo) && !boton.getStyleClass().contains("secundario")) {
                    boton.getStyleClass().add("secundario");
                }
            }
            if (panel.getScene() != null && panel.getScene().getWindow() instanceof Stage ventana
                    && ventana.getIcons().isEmpty()) {
                ventana.getIcons().add(MarcaQuilate.imagen(32));
            }
        });
        return dialogo;
    }

    static boolean esSecundario(ButtonType tipo) {
        ButtonData dato = tipo.getButtonData();
        return dato == ButtonData.CANCEL_CLOSE || dato == ButtonData.NO;
    }

    /**
     * La ventana de la app que tiene el foco; si ninguna lo tiene (por ejemplo, el aviso llega
     * al terminar una tarea en segundo plano), la última ventana visible que se abrió.
     */
    static Window ventanaActiva() {
        List<Window> visibles = Window.getWindows().stream().filter(Window::isShowing).toList();
        for (Window ventana : visibles) {
            if (ventana.isFocused()) {
                return ventana;
            }
        }
        return visibles.isEmpty() ? null : visibles.getLast();
    }
}
