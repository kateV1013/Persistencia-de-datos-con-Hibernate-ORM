package com.quilate.controller;

import com.quilate.dao.ReporteDAO;
import com.quilate.dto.PiezaMasVendida;
import com.quilate.util.Formatos;
import com.quilate.util.Tareas;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

/** Reporte de piezas mas vendidas (consulta HQL de ReporteDAO) con grafica y tabla. */
public class ReportePiezasController {

    @FXML private Spinner<Integer> spnLimite;
    @FXML private Label lblMensaje;
    @FXML private BarChart<String, Number> grafico;
    @FXML private TableView<PiezaMasVendida> tabla;
    @FXML private TableColumn<PiezaMasVendida, Void> colPosicion;
    @FXML private TableColumn<PiezaMasVendida, String> colCodigo;
    @FXML private TableColumn<PiezaMasVendida, String> colNombre;
    @FXML private TableColumn<PiezaMasVendida, String> colCantidad;
    @FXML private TableColumn<PiezaMasVendida, String> colTotal;

    private final ReporteDAO reporteDAO = new ReporteDAO();

    @FXML
    private void initialize() {
        spnLimite.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 50, 10));

        colPosicion.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().codigoBarras()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre()));
        colCantidad.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().cantidadVendida())));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(Formatos.dinero(c.getValue().totalVendido())));
        tabla.setPlaceholder(new Label("Todavía no hay ventas confirmadas"));

        generar(false);
    }

    @FXML
    private void onGenerar() {
        generar(true);
    }

    private void generar(boolean avisarErrores) {
        int limite = spnLimite.getValue() == null ? 10 : spnLimite.getValue();
        lblMensaje.setText("Generando...");
        Tareas.ejecutar(() -> reporteDAO.piezasMasVendidas(limite), this::mostrar, error -> {
            lblMensaje.setText("No se pudo generar el reporte: " + Tareas.mensajeCorto(error));
            if (avisarErrores) {
                Tareas.mostrarError(error);
            }
        });
    }

    private void mostrar(List<PiezaMasVendida> filas) {
        tabla.setItems(FXCollections.observableArrayList(filas));

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (PiezaMasVendida fila : filas) {
            serie.getData().add(new XYChart.Data<>(fila.nombre(), fila.cantidadVendida()));
        }
        grafico.getData().setAll(List.of(serie));

        lblMensaje.setText(filas.isEmpty() ? "Sin ventas confirmadas" : filas.size() + " pieza(s)");
    }
}
