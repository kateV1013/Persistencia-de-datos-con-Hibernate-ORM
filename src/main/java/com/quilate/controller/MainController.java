package com.quilate.controller;

import com.quilate.ui.Iconos;
import com.quilate.ui.Modulo;
import com.quilate.ui.MonedaVista;
import com.quilate.ui.Ventanas;
import com.quilate.util.JPAUtil;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class MainController {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es"));

    @FXML private Label lblFecha, lblEstado, sinResultados;
    @FXML private StackPane selectorMoneda, iconoBusqueda, portada, artePortada;
    @FXML private VBox seccionTienda, seccionTaller;
    @FXML private Region divisorSecciones;
    @FXML private HBox seccionesMenu;
    @FXML private Circle indicadorConexion;
    @FXML private Button registrarVenta;
    @FXML private ToggleButton vistaGeneral, vistaTienda;
    @FXML private TextField busqueda;
    @FXML private ScrollPane scroll;

    private final Map<Modulo, Button> filas = new EnumMap<>(Modulo.class);
    private Modulo.Seccion filtroSeccion;

    @FXML
    private void initialize() {
        String fecha = LocalDate.now().format(FORMATO_FECHA);
        lblFecha.setText(Character.toUpperCase(fecha.charAt(0)) + fecha.substring(1));
        artePortada.maxWidthProperty().bind(Bindings.min(602, portada.widthProperty().multiply(0.60)));
        selectorMoneda.getChildren().add(MonedaVista.selector());
        iconoBusqueda.getChildren().add(Iconos.trazo(Iconos.BUSCAR, 15));
        registrarVenta.setGraphic(Iconos.trazo(Iconos.FLECHA, 20));
        registrarVenta.setContentDisplay(ContentDisplay.RIGHT);

        for (Modulo modulo : Modulo.values()) {
            Button fila = crearFila(modulo);
            filas.put(modulo, fila);
            VBox grupo = modulo.seccion() == Modulo.Seccion.TIENDA ? seccionTienda : seccionTaller;
            grupo.getChildren().add(fila);
        }
        busqueda.textProperty().addListener((o, a, b) -> filtrar());
        verificarConexion();
    }

    @FXML
    private void cambiarSeccion(ActionEvent evento) {
        ToggleButton pestana = (ToggleButton) evento.getSource();
        pestana.setSelected(true);
        if (pestana == vistaGeneral) {
            filtroSeccion = null;
            busqueda.clear();
        } else {
            filtroSeccion = pestana == vistaTienda ? Modulo.Seccion.TIENDA : Modulo.Seccion.TALLER;
        }
        filtrar();
        scroll.setVvalue(scroll.getVmin());
    }

    private void filtrar() {
        String consulta = normalizar(busqueda.getText());
        boolean tiendaVisible = false;
        boolean tallerVisible = false;
        for (var entrada : filas.entrySet()) {
            Modulo modulo = entrada.getKey();
            boolean visible = (filtroSeccion == null || filtroSeccion == modulo.seccion())
                    && normalizar(modulo.titulo() + " " + modulo.descripcion() + " " + modulo.seccion().titulo())
                    .contains(consulta);
            mostrar(entrada.getValue(), visible);
            if (visible) {
                tiendaVisible |= modulo.seccion() == Modulo.Seccion.TIENDA;
                tallerVisible |= modulo.seccion() == Modulo.Seccion.TALLER;
            }
        }
        boolean hayResultados = tiendaVisible || tallerVisible;
        mostrar(seccionTienda, tiendaVisible);
        mostrar(seccionTaller, tallerVisible);
        mostrar(divisorSecciones, tiendaVisible && tallerVisible);
        mostrar(seccionesMenu, hayResultados);
        mostrar(sinResultados, !hayResultados);
    }

    private static void mostrar(Node nodo, boolean visible) {
        nodo.setVisible(visible);
        nodo.setManaged(visible);
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    @FXML
    private void abrirVentas() {
        Ventanas.abrir(Modulo.VENTAS);
    }

    private Button crearFila(Modulo modulo) {
        Label titulo = new Label(modulo.titulo());
        titulo.getStyleClass().add("fila-titulo");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        StackPane icono = new StackPane(Iconos.trazo(modulo.icono(), 28));
        icono.setMinWidth(36);
        HBox contenido = new HBox(18, icono, titulo, espacio, Iconos.trazo(Iconos.FLECHA, 18));
        contenido.setAlignment(Pos.CENTER_LEFT);

        Button fila = new Button(null, contenido);
        fila.getStyleClass().add("fila-modulo");
        fila.setMaxWidth(Double.MAX_VALUE);
        fila.setAccessibleText("Abrir " + modulo.titulo());
        fila.setTooltip(new Tooltip(modulo.descripcion()));
        fila.setOnAction(e -> Ventanas.abrir(modulo));
        return fila;
    }

    private void verificarConexion() {
        lblEstado.setText("Conectando…");
        indicadorConexion.getStyleClass().setAll("indicador", "indicador-pendiente");

        Task<Void> conexion = new Task<>() {
            @Override
            protected Void call() {
                var em = JPAUtil.getEntityManager();
                try {
                    em.createNativeQuery("select 1").getSingleResult();
                } finally {
                    em.close();
                }
                return null;
            }
        };
        conexion.setOnSucceeded(e -> {
            lblEstado.setText("Conectado a la base de datos");
            indicadorConexion.getStyleClass().setAll("indicador", "indicador-ok");
        });
        conexion.setOnFailed(e -> {
            Throwable err = conexion.getException();
            while (err.getCause() != null) {
                err = err.getCause();
            }
            lblEstado.setText("Sin conexión · revisa la configuración de la base de datos");
            lblEstado.setTooltip(new Tooltip(err.getMessage()));
            indicadorConexion.getStyleClass().setAll("indicador", "indicador-error");
        });

        Thread hilo = new Thread(conexion, "conexion-bd");
        hilo.setDaemon(true);
        hilo.start();
    }
}
