package com.quilate.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** Moneda de presentación para la sesión. Los importes del modelo se mantienen en NIO. */
public final class MonedaVista {
    private record Preferencia(boolean dolares, BigDecimal tasa) { }

    // Tipo de cambio oficial publicado por el BCN para 2026: https://bcn.gob.ni/node/10376
    private static final SimpleObjectProperty<Preferencia> PREFERENCIA =
            new SimpleObjectProperty<>(new Preferencia(false, new BigDecimal("36.6243")));

    private MonedaVista() { }

    /** true si el usuario eligió ver los importes en dólares. */
    public static boolean enDolares() {
        return PREFERENCIA.get().dolares();
    }

    /** Córdobas por US$ 1 que se usan para convertir. */
    public static BigDecimal tasa() {
        return PREFERENCIA.get().tasa();
    }

    public static String formatear(BigDecimal cordobas) {
        Preferencia p = PREFERENCIA.get();
        return formatear(cordobas, p.dolares(), p.tasa());
    }

    static String formatear(BigDecimal cordobas, boolean dolares, BigDecimal tasa) {
        if (cordobas == null) return "";
        var formato = NumberFormat.getNumberInstance(Locale.US);
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        BigDecimal importe = dolares ? cordobas.divide(tasa, 2, RoundingMode.HALF_UP) : cordobas;
        return (dolares ? "US$ " : "C$ ") + formato.format(importe);
    }

    public static StringBinding importe(BigDecimal cordobas) {
        return Bindings.createStringBinding(() -> formatear(cordobas), PREFERENCIA);
    }

    public static void observar(Node nodo, Runnable actualizar) {
        InvalidationListener listener = cambio -> actualizar.run();
        nodo.getProperties().put(listener, listener);
        PREFERENCIA.addListener(new WeakInvalidationListener(listener));
    }

    public static MenuButton selector() {
        MenuButton menu = new MenuButton();
        menu.textProperty().bind(Bindings.createStringBinding(() ->
                PREFERENCIA.get().dolares() ? "Dólares (US$)" : "Córdobas (C$)", PREFERENCIA));
        menu.setAccessibleText("Moneda de visualización");
        menu.getStyleClass().add("selector-moneda");

        MenuItem cordobas = new MenuItem("Córdobas (C$)");
        cordobas.setOnAction(e -> PREFERENCIA.set(new Preferencia(false, PREFERENCIA.get().tasa())));
        MenuItem dolares = new MenuItem("Dólares (US$)");
        dolares.setOnAction(e -> PREFERENCIA.set(new Preferencia(true, PREFERENCIA.get().tasa())));
        MenuItem tasa = new MenuItem();
        tasa.textProperty().bind(Bindings.createStringBinding(
                () -> "Cambiar tasa · C$ " + PREFERENCIA.get().tasa().toPlainString() + " por US$ 1", PREFERENCIA));
        tasa.setOnAction(e -> editarTasa(menu));
        menu.getItems().addAll(cordobas, dolares, tasa);
        return menu;
    }

    private static void editarTasa(MenuButton menu) {
        Dialog<BigDecimal> dialogo = new Dialog<>();
        dialogo.initOwner(menu.getScene().getWindow());
        dialogo.setTitle("Tipo de cambio");
        dialogo.setHeaderText("Córdobas por US$ 1");
        TextField campo = new TextField(PREFERENCIA.get().tasa().toPlainString());
        Label ayuda = new Label("Esta tasa se usa para mostrar equivalencias durante la sesión.");
        VBox contenido = new VBox(10, campo, ayuda);
        contenido.setPrefWidth(365);
        var panel = dialogo.getDialogPane();
        panel.setContent(contenido);
        panel.getStylesheets().add(Ventanas.hojaEstilos());
        ButtonType aplicar = new ButtonType("Aplicar", ButtonBar.ButtonData.OK_DONE);
        panel.getButtonTypes().addAll(aplicar, ButtonType.CANCEL);
        panel.lookupButton(aplicar).disableProperty().bind(
                Bindings.createBooleanBinding(() -> leerTasa(campo.getText()) == null, campo.textProperty()));
        dialogo.setResultConverter(boton -> boton == aplicar ? leerTasa(campo.getText()) : null);
        Dialogos.preparar(dialogo);
        dialogo.showAndWait().ifPresent(valor ->
                PREFERENCIA.set(new Preferencia(PREFERENCIA.get().dolares(), valor)));
    }

    static BigDecimal leerTasa(String texto) {
        String valor = texto.strip().replace(',', '.');
        if (!valor.matches("[0-9]{1,6}(?:[.][0-9]{1,6})?")) return null;
        BigDecimal tasa = new BigDecimal(valor);
        return tasa.signum() > 0 ? tasa : null;
    }
}
