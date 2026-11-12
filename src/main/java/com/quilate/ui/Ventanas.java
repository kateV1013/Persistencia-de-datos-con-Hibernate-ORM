package com.quilate.ui;

import com.quilate.util.AlertUtil;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Abre cada modulo en su propia ventana. Si el modulo ya esta abierto, trae
 * esa ventana al frente en lugar de duplicarla (asi no hay dos ventanas de
 * Ventas con datos distintos).
 */
public final class Ventanas {

    public static final String HOJA_ESTILOS = "/css/styles.css";

    private static final Map<Modulo, Stage> ABIERTAS = new EnumMap<>(Modulo.class);
    private static final double DESPLAZAMIENTO_CASCADA = 28;

    private Ventanas() {
    }

    public static void abrir(Modulo modulo) {
        Stage existente = ABIERTAS.get(modulo);
        if (existente != null) {
            existente.setIconified(false);
            existente.toFront();
            existente.requestFocus();
            return;
        }

        Parent contenido;
        try {
            contenido = FXMLLoader.load(Objects.requireNonNull(
                    Ventanas.class.getResource(modulo.fxml()), modulo.fxml()));
        } catch (Exception e) {
            AlertUtil.error(e);
            return;
        }
        contenido.getStyleClass().add("contenido-modulo");

        BorderPane raiz = new BorderPane(contenido);
        raiz.setTop(encabezado(modulo));
        raiz.getStyleClass().add("ventana-modulo");
        raiz.setStyle(modulo.estiloAcento());

        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        double ancho = Math.min(1120, pantalla.getWidth() - 40);
        double alto = Math.min(760, pantalla.getHeight() - 40);

        Stage ventana = new Stage();
        ventana.setTitle(modulo.titulo() + " · Quilate");
        ventana.setScene(crearEscena(raiz, ancho, alto));
        ventana.setMinWidth(760);
        ventana.setMinHeight(520);
        prepararVentana(ventana);
        ventana.setOnHidden(e -> ABIERTAS.remove(modulo));

        int yaAbiertas = ABIERTAS.size();
        ABIERTAS.put(modulo, ventana);
        ventana.show();
        ventana.setX(ventana.getX() + DESPLAZAMIENTO_CASCADA * yaAbiertas);
        ventana.setY(ventana.getY() + DESPLAZAMIENTO_CASCADA * yaAbiertas);

        Animaciones.entrada(contenido, Duration.millis(60));
    }

    /** Cierra todas las ventanas de modulos (al cerrar el menu principal). */
    public static void cerrarTodas() {
        for (Stage ventana : new ArrayList<>(ABIERTAS.values())) {
            ventana.close();
        }
    }

    public static Scene crearEscena(Parent raiz, double ancho, double alto) {
        Scene escena = new Scene(raiz, ancho, alto);
        escena.getStylesheets().add(hojaEstilos());
        return escena;
    }

    public static String hojaEstilos() {
        return Objects.requireNonNull(Ventanas.class.getResource(HOJA_ESTILOS)).toExternalForm();
    }

    /** Icono de la aplicacion en la barra de titulo y de tareas. */
    public static void prepararVentana(Stage ventana) {
        ventana.getIcons().addAll(iconosAplicacion());
    }

    private static List<Image> iconos;

    private static List<Image> iconosAplicacion() {
        if (iconos == null) {
            iconos = List.of(Iconos.imagenAplicacion(16), Iconos.imagenAplicacion(32),
                    Iconos.imagenAplicacion(64));
        }
        return iconos;
    }

    private static HBox encabezado(Modulo modulo) {
        Label titulo = new Label(modulo.titulo());
        titulo.getStyleClass().add("encabezado-modulo-titulo");
        Label descripcion = new Label(modulo.descripcion());
        descripcion.getStyleClass().add("encabezado-modulo-descripcion");

        HBox encabezado = new HBox(14, Iconos.enCirculo(modulo.icono(), 44), new VBox(2, titulo, descripcion));
        encabezado.setAlignment(Pos.CENTER_LEFT);
        encabezado.getStyleClass().add("encabezado-modulo");
        return encabezado;
    }
}
