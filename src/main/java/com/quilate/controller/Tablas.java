package com.quilate.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.*;
import java.util.function.Function;

final class Tablas {
    private Tablas() {}
    static <T> void columna(TableView<T> tabla, String titulo, Function<T, ?> valor) {
        TableColumn<T, Object> c = new TableColumn<>(titulo);
        c.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(valor.apply(f.getValue())));
        c.setPrefWidth(145);
        tabla.getColumns().add(c);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPlaceholder(new Label("Todavía no hay registros para mostrar"));
    }
}
