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

    @FXML private Label lblFecha;
    @FXML private VBox contenedorSecciones;
    @FXML private Circle indicadorConexion;
    @FXML private Label lblEstado;

    @FXML
    private void initialize() {
        String fecha = LocalDate.now().format(FORMATO_FECHA);
        lblFecha.setText(Character.toUpperCase(fecha.charAt(0)) + fecha.substring(1));

        construirMenu();
        verificarConexion();
    }

    @FXML private javafx.scene.layout.BorderPane raiz;
    @FXML private VBox lateral, navegacion;
    @FXML private javafx.scene.control.TextField busqueda;
    @FXML private javafx.scene.control.ScrollPane scroll;
    @FXML private Label sinResultados;
    private final java.util.Map<Modulo, Button> tarjetasModulo = new java.util.EnumMap<>(Modulo.class);
    private final java.util.Map<Modulo.Seccion, VBox> secciones = new java.util.EnumMap<>(Modulo.Seccion.class);

    private void construirMenu() {
        for (Modulo.Seccion seccion : Modulo.Seccion.values()) {
            Label navTitulo = new Label(seccion.titulo().toUpperCase(Locale.forLanguageTag("es")));
            navTitulo.getStyleClass().add("nav-seccion");
            navegacion.getChildren().add(navTitulo);
            Label titulo = new Label(seccion.titulo());
            titulo.getStyleClass().add("titulo-seccion-menu");
            TilePane tarjetas = new TilePane();
            tarjetas.getStyleClass().add("tarjetas");
            tarjetas.setAlignment(Pos.TOP_LEFT);
            // El número de columnas se adapta al espacio disponible.
            tarjetas.prefTileWidthProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(() -> {
                double ancho = Math.max(230, contenedorSecciones.getWidth() - 56);
                int columnas = ancho >= 840 ? 4 : ancho >= 610 ? 3 : ancho >= 410 ? 2 : 1;
                return Math.floor((ancho - (columnas - 1) * 14) / columnas);
            }, contenedorSecciones.widthProperty()));
            for (Modulo modulo : Modulo.values()) {
                if (modulo.seccion() != seccion) continue;
                Button tarjeta = crearTarjeta(modulo);
                tarjeta.prefWidthProperty().bind(tarjetas.prefTileWidthProperty());
                tarjeta.setMaxWidth(Double.MAX_VALUE);
                tarjetas.getChildren().add(tarjeta);
                tarjetasModulo.put(modulo, tarjeta);
                Button enlace = new Button(modulo.titulo(), Iconos.crear(modulo.icono(), 17, Color.web("#C5D6D0")));
                enlace.getStyleClass().add("nav-boton");
                enlace.setMaxWidth(Double.MAX_VALUE);
                enlace.setOnAction(e -> Ventanas.abrir(modulo));
                navegacion.getChildren().add(enlace);
            }
            VBox grupo = new VBox(10, titulo, tarjetas);
            secciones.put(seccion, grupo);
            contenedorSecciones.getChildren().add(grupo);
        }
        busqueda.textProperty().addListener((o, a, b) -> filtrar(b));
        raiz.widthProperty().addListener((o, a, b) -> {
            boolean mostrar = b.doubleValue() >= 1080;
            lateral.setVisible(mostrar);
            lateral.setManaged(mostrar);
        });
    }

    private void filtrar(String texto) {
        String consulta = normalizar(texto);
        tarjetasModulo.forEach((modulo, boton) -> {
            boolean coincide = normalizar(modulo.titulo() + " " + modulo.descripcion()
                    + " " + modulo.seccion().titulo()).contains(consulta);
            boton.setVisible(coincide);
            boton.setManaged(coincide);
        });
        secciones.forEach((seccion, grupo) -> {
            boolean visible = tarjetasModulo.entrySet().stream()
                    .anyMatch(e -> e.getKey().seccion() == seccion && e.getValue().isVisible());
            grupo.setVisible(visible);
            grupo.setManaged(visible);
        });
        boolean vacio = tarjetasModulo.values().stream().noneMatch(Button::isVisible);
        sinResultados.setVisible(vacio);
        sinResultados.setManaged(vacio);
    }

    private String normalizar(String texto) {
        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    @FXML private void abrirVentas() { Ventanas.abrir(Modulo.VENTAS); }
    @FXML private void irInicio() { busqueda.clear(); scroll.setVvalue(0); }

    private Button crearTarjeta(Modulo modulo) {
        Label titulo = new Label(modulo.titulo());
        titulo.getStyleClass().add("tarjeta-titulo");
        titulo.setWrapText(true);
        Label descripcion = new Label(modulo.descripcion());
        descripcion.getStyleClass().add("tarjeta-descripcion");
        descripcion.setWrapText(true);
        javafx.scene.layout.Region espacio = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(espacio, javafx.scene.layout.Priority.ALWAYS);
        Label flecha = new Label("↗");
        flecha.getStyleClass().add("tarjeta-flecha");
        javafx.scene.layout.HBox fila = new javafx.scene.layout.HBox(Iconos.enCirculo(modulo.icono(), 34), espacio, flecha);
        fila.setAlignment(Pos.CENTER_LEFT);
        VBox contenido = new VBox(9, fila, titulo, descripcion);
        contenido.setAlignment(Pos.TOP_LEFT);
        Button tarjeta = new Button();
        tarjeta.setGraphic(contenido);
        tarjeta.getStyleClass().setAll("tarjeta-modulo");
        tarjeta.setStyle(modulo.estiloAcento());
        tarjeta.setAlignment(Pos.TOP_LEFT);
        tarjeta.setAccessibleText("Abrir " + modulo.titulo());
        tarjeta.setTooltip(new Tooltip(modulo.descripcion()));
        tarjeta.setOnAction(e -> Ventanas.abrir(modulo));
        return tarjeta;
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
