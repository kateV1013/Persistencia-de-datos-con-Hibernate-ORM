package com.quilate;

import com.quilate.ui.MonedaVista;
import com.quilate.ui.Ventanas;
import com.quilate.util.JPAUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));

        stage.getIcons().addAll(com.quilate.ui.MarcaQuilate.imagen(32), com.quilate.ui.MarcaQuilate.imagen(64));
        stage.setTitle("Joyería Quilate · Menú principal");
        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        double alto = Math.min(680, pantalla.getHeight() - 40);
        stage.setScene(Ventanas.crearEscena(loader.load(), Math.min(1020, pantalla.getWidth() - 40), alto));
        stage.setMinWidth(Math.min(900, pantalla.getWidth() - 20));
        stage.setMinHeight(Math.min(650, pantalla.getHeight() - 20));
        // Al cerrar el menu principal se cierran tambien las ventanas de los modulos.
        stage.setOnHidden(e -> {
            Ventanas.cerrarTodas();
            Platform.exit();
        });
        stage.show();
        MonedaVista.cargarTasas();
    }

    @Override
    public void stop() {
        JPAUtil.cerrar();
    }

    public static void main(String[] args) {
        // Botones de diálogo («Aceptar», «Cancelar», «Sí») y calendario en español aunque
        // Windows o macOS estén en otro idioma. Los importes ya usan su formato fijo (C$ 1,500.00).
        java.util.Locale.setDefault(java.util.Locale.forLanguageTag("es-NI"));
        launch(args);
    }
}
