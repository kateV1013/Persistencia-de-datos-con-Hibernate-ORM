package com.quilate.ui;

import com.quilate.util.ReporteUtil;
import com.quilate.util.Tareas;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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
        dialogo.setTitle(titulo + " · Joyería Quilate");
        // El nombre del reporte como encabezado; la instrucción, corta y en varias líneas.
        dialogo.setHeaderText(titulo);
        Label ayuda = new Label("Elige el período (dd/mm/aaaa). Deja una fecha vacía para no limitar ese extremo.");
        ayuda.setWrapText(true);
        ayuda.getStyleClass().add("ayuda");

        DatePicker desde = new DatePicker(desdeInicial);
        DatePicker hasta = new DatePicker(hastaInicial);
        desde.setMaxWidth(Double.MAX_VALUE);
        hasta.setMaxWidth(Double.MAX_VALUE);
        Label etiquetaDesde = new Label("DESDE");
        Label etiquetaHasta = new Label("HASTA");
        etiquetaDesde.getStyleClass().add("campo-label");
        etiquetaHasta.getStyleClass().add("campo-label");
        GridPane campos = new GridPane();
        campos.setHgap(12);
        campos.setVgap(10);
        ColumnConstraints columnaEtiqueta = new ColumnConstraints();
        ColumnConstraints columnaCampo = new ColumnConstraints();
        columnaCampo.setHgrow(Priority.ALWAYS);
        campos.getColumnConstraints().addAll(columnaEtiqueta, columnaCampo);
        campos.addRow(0, etiquetaDesde, desde);
        campos.addRow(1, etiquetaHasta, hasta);

        // Atajos para los períodos más usados.
        LocalDate hoy = LocalDate.now();
        FlowPane atajos = new FlowPane(8, 8,
                atajo("Este mes", desde, hasta, hoy.withDayOfMonth(1), hoy.withDayOfMonth(hoy.lengthOfMonth())),
                atajo("Últimos 30 días", desde, hasta, hoy.minusDays(29), hoy),
                atajo("Todo el historial", desde, hasta, null, null));

        VBox contenido = new VBox(14, ayuda, campos, atajos);
        contenido.setPrefWidth(380);
        contenido.setPadding(new javafx.geometry.Insets(6, 6, 2, 6));
        dialogo.getDialogPane().setContent(contenido);
        // Mismo formato y validación de fechas que el resto de formularios (dd/mm/aaaa estricto).
        FormulariosVisuales.aplicar(desde);
        FormulariosVisuales.aplicar(hasta);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Dialogos.preparar(dialogo);
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

    private static Button atajo(String texto, DatePicker desde, DatePicker hasta, LocalDate inicio, LocalDate fin) {
        Button boton = new Button(texto);
        boton.getStyleClass().addAll("secundario", "atajo-periodo");
        boton.setOnAction(e -> {
            desde.setValue(inicio);
            hasta.setValue(fin);
            // El DatePicker conserva el texto escrito a mano; se sincroniza con el valor nuevo.
            desde.getEditor().setText(inicio == null ? "" : desde.getConverter().toString(inicio));
            hasta.getEditor().setText(fin == null ? "" : hasta.getConverter().toString(fin));
        });
        return boton;
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
