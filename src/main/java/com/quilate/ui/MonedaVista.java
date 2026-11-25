package com.quilate.ui;

import com.quilate.service.FrankfurterCliente;
import com.quilate.util.Tareas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/** Moneda de presentación para la sesión. Los importes del modelo se mantienen en NIO. */
public final class MonedaVista {

    public record Moneda(String codigo, String nombre, String simbolo) {
        @Override
        public String toString() {
            return nombre + " (" + simbolo + ")";
        }
    }

    public static final Moneda CORDOBA = new Moneda("NIO", "Córdobas", "C$");
    public static final List<Moneda> MONEDAS = List.of(
            CORDOBA, new Moneda("USD", "Dólares", "US$"), new Moneda("EUR", "Euros", "€"));

    private record Preferencia(Moneda moneda, Map<String, BigDecimal> tasas, boolean enLinea) { }

    // Sin internet se usa el tipo de cambio oficial del BCN para 2026: https://bcn.gob.ni/node/10376
    private static final SimpleObjectProperty<Preferencia> PREFERENCIA = new SimpleObjectProperty<>(
            new Preferencia(CORDOBA, Map.of("NIO", BigDecimal.ONE, "USD", new BigDecimal("36.6243")), false));

    private MonedaVista() { }

    public static Moneda moneda() {
        return PREFERENCIA.get().moneda();
    }

    /** Córdobas que vale una unidad de la moneda elegida. */
    public static BigDecimal tasa() {
        return tasa(moneda().codigo());
    }

    private static BigDecimal tasa(String codigo) {
        return PREFERENCIA.get().tasas().get(codigo);
    }

    public static BigDecimal dolaresACordobas(BigDecimal dolares) {
        return dolares.multiply(tasa("USD"));
    }

    public static void cargarTasas() {
        Tareas.ejecutar(() -> {
            FrankfurterCliente frankfurter = new FrankfurterCliente();
            Map<String, BigDecimal> tasas = new HashMap<>();
            for (Moneda m : MONEDAS) {
                tasas.put(m.codigo(), m == CORDOBA ? BigDecimal.ONE : frankfurter.tasa(m.codigo(), "NIO"));
            }
            return Map.copyOf(tasas);
        }, tasas -> PREFERENCIA.set(new Preferencia(moneda(), tasas, true)), error -> { });
    }

    public static String formatear(BigDecimal cordobas) {
        return formatear(cordobas, moneda(), tasa());
    }

    static String formatear(BigDecimal cordobas, Moneda moneda, BigDecimal tasa) {
        if (cordobas == null) return "";
        var formato = NumberFormat.getNumberInstance(Locale.US);
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        return moneda.simbolo() + " " + formato.format(cordobas.divide(tasa, 2, RoundingMode.HALF_UP));
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
        menu.textProperty().bind(Bindings.createStringBinding(() -> moneda().toString(), PREFERENCIA));
        menu.setAccessibleText("Moneda de visualización");
        menu.getStyleClass().add("selector-moneda");

        for (Moneda m : MONEDAS) {
            MenuItem opcion = new MenuItem();
            opcion.textProperty().bind(Bindings.createStringBinding(() -> etiqueta(m), PREFERENCIA));
            opcion.disableProperty().bind(Bindings.createBooleanBinding(() -> tasa(m.codigo()) == null, PREFERENCIA));
            opcion.setOnAction(e -> PREFERENCIA.set(new Preferencia(m, PREFERENCIA.get().tasas(), PREFERENCIA.get().enLinea())));
            menu.getItems().add(opcion);
        }
        MenuItem origen = new MenuItem();
        origen.setDisable(true);
        origen.textProperty().bind(Bindings.createStringBinding(() -> PREFERENCIA.get().enLinea()
                ? "Tasas del día · Frankfurter" : "Sin conexión · tasa fija del BCN", PREFERENCIA));
        menu.getItems().addAll(new SeparatorMenuItem(), origen);
        return menu;
    }

    private static String etiqueta(Moneda m) {
        if (m == CORDOBA) return m.toString();
        BigDecimal tasa = tasa(m.codigo());
        if (tasa == null) return m + " · sin conexión";
        return m + " · C$ " + tasa.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
