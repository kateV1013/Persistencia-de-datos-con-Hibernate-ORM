package com.quilate.ui;

import com.quilate.util.ReporteUtil;
import com.quilate.util.Tareas;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.Optional;

/** Pide un rango de fechas para un reporte. Por defecto, los últimos 30 días. */
public final class DialogoPeriodo {

    /** Rango elegido; cualquiera de las dos fechas puede quedar vacía. */
    public record Periodo(LocalDate desde, LocalDate hasta) { }

    private DialogoPeriodo() {
    }

    /** Período por defecto: los últimos 30 días. */
    public static Optional<Periodo> pedir(String titulo) {
        return pedir(titulo, LocalDate.now().minusDays(29), LocalDate.now());
    }

    public static Optional<Periodo> pedir(String titulo, LocalDate desdeInicial, LocalDate hastaInicial) {
        Dialog<Periodo> dialogo = new Dialog<>();
        dialogo.setTitle(titulo);
        dialogo.setHeaderText("Elige el período del reporte (dd/mm/aaaa). Deja una fecha vacía para no limitar ese extremo.");
        DatePicker desde = new DatePicker(desdeInicial);
        DatePicker hasta = new DatePicker(hastaInicial);
        GridPane campos = new GridPane();
        campos.setHgap(10);
        campos.setVgap(10);
        campos.addRow(0, new Label("Desde"), desde);
        campos.addRow(1, new Label("Hasta"), hasta);
        dialogo.getDialogPane().setContent(campos);
        // Mismo formato y validación de fechas que el resto de formularios (dd/mm/aaaa estricto).
        FormulariosVisuales.aplicar(desde);
        FormulariosVisuales.aplicar(hasta);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        var css = DialogoPeriodo.class.getResource("/css/styles.css");
        if (css != null) dialogo.getDialogPane().getStylesheets().add(css.toExternalForm());
        dialogo.setResultConverter(boton -> {
            if (boton != ButtonType.OK) return null;
            try {
                // Toma también una fecha escrita a mano aunque no se haya pulsado Enter.
                desde.commitValue();
                hasta.commitValue();
            } catch (RuntimeException e) {
                com.quilate.util.AlertUtil.advertencia("Escribe las fechas como dd/mm/aaaa, por ejemplo 01/11/2026.");
                return null;
            }
            return new Periodo(desde.getValue(), hasta.getValue());
        });
        return dialogo.showAndWait();
    }

    /**
     * Pide el período y abre el reporte.
     *
     * @param conHora true si filtra una columna fecha-hora (ventas, apartados);
     *                false si filtra una columna DATE (pedidos)
     */
    public static void abrirReporte(String rutaJrxml, String titulo, boolean conHora) {
        // Los pedidos del taller miran también hacia adelante: por defecto, del último mes a dos meses.
        Optional<Periodo> elegido = conHora
                ? pedir(titulo)
                : pedir(titulo, LocalDate.now().minusDays(29), LocalDate.now().plusMonths(2));
        elegido.ifPresent(periodo -> {
            try {
                var parametros = conHora
                        ? ReporteUtil.rangoFechaHora(periodo.desde(), periodo.hasta())
                        : ReporteUtil.rangoFecha(periodo.desde(), periodo.hasta());
                ReporteUtil.mostrarReporte(rutaJrxml, parametros, titulo);
            } catch (RuntimeException e) {
                Tareas.mostrarError(e);
            }
        });
    }
}
