package com.quilate.ui;

import com.quilate.util.AlertUtil;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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
    /** Cmd+M en Mac, Ctrl+M en Windows y Linux. */
    private static final KeyCombination MINIMIZAR = new KeyCodeCombination(KeyCode.M, KeyCombination.SHORTCUT_DOWN);

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
        Object controlador;
        try {
            FXMLLoader cargador = new FXMLLoader(Objects.requireNonNull(
                    Ventanas.class.getResource(modulo.fxml()), modulo.fxml()));
            contenido = cargador.load();
            controlador = cargador.getController();
        } catch (Exception e) {
            AlertUtil.error(e);
            return;
        }
        contenido.getStyleClass().add("contenido-modulo");

        BorderPane raiz = new BorderPane(contenido);
        raiz.setTop(encabezado(modulo));
        raiz.getStyleClass().add("ventana-modulo");

        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        double ancho = Math.min(1340, pantalla.getWidth() - 40);
        double alto = Math.min(860, pantalla.getHeight() - 40);

        Stage ventana = new Stage();
        ventana.getIcons().add(com.quilate.ui.MarcaQuilate.imagen(32));
        ventana.setTitle(modulo.titulo() + " · Joyería Quilate");
        ventana.setScene(crearEscena(raiz, ancho, alto));
        // En pantallas pequeñas (por ejemplo 1366×768 con escala 125 % en Windows) el mínimo
        // no puede pasar del área visible, o la parte de abajo quedaría fuera de la pantalla.
        ventana.setMinWidth(Math.min(1040, pantalla.getWidth() - 20));
        ventana.setMinHeight(Math.min(680, pantalla.getHeight() - 20));
        ventana.setOnHidden(e -> ABIERTAS.remove(modulo));
        atajosDeVentana(ventana);
        if (controlador instanceof Actualizable actualizable) {
            recargarAlVolver(ventana, modulo, actualizable);
        }
        // La pantalla ya cargó sus datos al abrirse: no hace falta recargarla al recibir el foco.
        ultimoModulo = modulo;

        int yaAbiertas = ABIERTAS.size();
        ABIERTAS.put(modulo, ventana);
        ventana.show();
        ventana.setX(Math.max(pantalla.getMinX(), Math.min(ventana.getX() + DESPLAZAMIENTO_CASCADA * yaAbiertas,
                pantalla.getMaxX() - ventana.getWidth())));
        ventana.setY(Math.max(pantalla.getMinY(), Math.min(ventana.getY() + DESPLAZAMIENTO_CASCADA * yaAbiertas,
                pantalla.getMaxY() - ventana.getHeight())));

        Animaciones.entrada(contenido, Duration.millis(60));
    }

    /**
     * Esc cierra la ventana del módulo y Cmd/Ctrl + M la minimiza.
     * Se usa un manejador (no un filtro): si un control ya usó la tecla Esc
     * (por ejemplo, para cerrar una lista desplegable o cancelar una edición),
     * la ventana no se cierra.
     */
    static void atajosDeVentana(Stage ventana) {
        ventana.getScene().addEventHandler(KeyEvent.KEY_PRESSED, evento -> {
            if (evento.getCode() == KeyCode.ESCAPE) {
                evento.consume();
                ventana.close();
            } else if (MINIMIZAR.match(evento)) {
                evento.consume();
                ventana.setIconified(true);
            }
        });
    }

    /** Cierra todas las ventanas de modulos (al cerrar el menu principal). */
    /** Último módulo en el que estuvo el usuario; los diálogos y el menú no lo cambian. */
    private static Modulo ultimoModulo;

    /**
     * Al volver a esta ventana desde OTRO módulo, la pantalla relee sus datos. Volver de un
     * diálogo propio, del visor de reportes o de otra aplicación no recarga nada.
     */
    private static void recargarAlVolver(Stage ventana, Modulo modulo, Actualizable pantalla) {
        ventana.focusedProperty().addListener((obs, antes, enfocada) -> {
            if (!enfocada) return;
            if (debeRecargar(ultimoModulo, modulo)) {
                pantalla.actualizarAlVolver();
            }
            ultimoModulo = modulo;
        });
    }

    /** Solo al llegar desde otro módulo; volver a la misma ventana (tras un diálogo) no recarga. */
    static boolean debeRecargar(Modulo anterior, Modulo actual) {
        return anterior != null && anterior != actual;
    }

    public static void cerrarTodas() {
        for (Stage ventana : new ArrayList<>(ABIERTAS.values())) {
            ventana.close();
        }
    }

    public static Scene crearEscena(Parent raiz, double ancho, double alto) {
        cargarFuentes();
        PresentacionTablas.aplicar(raiz);
        FormulariosVisuales.aplicar(raiz);
        Scene escena = new Scene(raiz, ancho, alto);
        escena.getStylesheets().add(hojaEstilos());
        return escena;
    }

    private static boolean fuentesCargadas;
    private static void cargarFuentes() {
        if (fuentesCargadas) return;
        for (String fuente : List.of("OpenSans-Regular.ttf", "OpenSans-Bold.ttf")) {
            try (var archivo = Ventanas.class.getResourceAsStream("/fonts/" + fuente)) {
                if (archivo != null) javafx.scene.text.Font.loadFont(archivo, 12);
            } catch (java.io.IOException e) {
                // JavaFX conserva su fuente de sistema si el recurso no está disponible.
            }
        }
        fuentesCargadas = true;
    }

    public static String hojaEstilos() {
        return Objects.requireNonNull(Ventanas.class.getResource(HOJA_ESTILOS)).toExternalForm();
    }

    private static HBox encabezado(Modulo modulo) {
        Label titulo = new Label(modulo.titulo());
        titulo.getStyleClass().add("encabezado-modulo-titulo");
        Label descripcion = new Label(modulo.descripcion());
        descripcion.getStyleClass().add("encabezado-modulo-descripcion");

        StackPane emblema = new StackPane(Iconos.trazo(modulo.icono(), 26));
        emblema.getStyleClass().add("emblema-modulo");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        Label marca = new Label("Quilate");
        marca.getStyleClass().add("modulo-marca");
        HBox encabezado = new HBox(14, emblema, new VBox(2, titulo, descripcion),
                espacio, new MarcaQuilate(32), marca);
        encabezado.setAlignment(Pos.CENTER_LEFT);
        encabezado.getStyleClass().add("encabezado-modulo");
        return encabezado;
    }
}
