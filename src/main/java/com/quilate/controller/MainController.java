package com.quilate.controller;

import com.quilate.ui.Animaciones;
import com.quilate.ui.Iconos;
import com.quilate.ui.Modulo;
import com.quilate.ui.Ventanas;
import com.quilate.util.JPAUtil;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Menu principal: muestra una tarjeta por cada {@link Modulo}. Al pulsarla,
 * el modulo se abre en su propia ventana.
 */
public class MainController {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es"));

    @FXML private StackPane logo;
    @FXML private Label lblFecha;
    @FXML private VBox contenedorSecciones;
    @FXML private Circle indicadorConexion;
    @FXML private Label lblEstado;

    @FXML
    private void initialize() {
        logo.getChildren().add(Iconos.crear(Iconos.DIAMANTE, 30, Color.web("#D4AF37")));
        String fecha = LocalDate.now().format(FORMATO_FECHA);
        lblFecha.setText(Character.toUpperCase(fecha.charAt(0)) + fecha.substring(1));

        construirMenu();
        verificarConexion();
    }

    private void construirMenu() {
        int indice = 0;
        for (Modulo.Seccion seccion : Modulo.Seccion.values()) {
            Label titulo = new Label(seccion.titulo());
            titulo.getStyleClass().add("titulo-seccion-menu");

            TilePane tarjetas = new TilePane();
            tarjetas.getStyleClass().add("tarjetas");
            tarjetas.setAlignment(Pos.TOP_LEFT);

            for (Modulo modulo : Modulo.values()) {
                if (modulo.seccion() == seccion) {
                    Button tarjeta = crearTarjeta(modulo);
                    tarjetas.getChildren().add(tarjeta);
                    Animaciones.entrada(tarjeta, Duration.millis(80 + 55 * indice++));
                }
            }
            contenedorSecciones.getChildren().addAll(titulo, tarjetas);
        }
    }

    private Button crearTarjeta(Modulo modulo) {
        Label titulo = new Label(modulo.titulo());
        titulo.getStyleClass().add("tarjeta-titulo");
        titulo.setWrapText(true);
        Label descripcion = new Label(modulo.descripcion());
        descripcion.getStyleClass().add("tarjeta-descripcion");
        descripcion.setWrapText(true);

        VBox contenido = new VBox(10, Iconos.enCirculo(modulo.icono(), 56), titulo, descripcion);
        contenido.setAlignment(Pos.TOP_LEFT);

        Button tarjeta = new Button();
        tarjeta.setGraphic(contenido);
        tarjeta.getStyleClass().setAll("tarjeta-modulo");
        tarjeta.setStyle(modulo.estiloAcento());
        tarjeta.setAlignment(Pos.TOP_LEFT);
        tarjeta.setTooltip(new Tooltip("Abrir " + modulo.titulo() + " en una ventana nueva"));
        tarjeta.setOnAction(e -> Ventanas.abrir(modulo));
        Animaciones.escalarAlPasar(tarjeta, 1.03);
        return tarjeta;
    }

    private void verificarConexion() {
        lblEstado.setText("Conectando a PostgreSQL...");
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
            lblEstado.setText("Sin conexión: " + err.getMessage());
            indicadorConexion.getStyleClass().setAll("indicador", "indicador-error");
        });

        Thread hilo = new Thread(conexion, "conexion-bd");
        hilo.setDaemon(true);
        hilo.start();
    }
}
