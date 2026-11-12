package com.quilate;

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

        stage.setTitle("Quilate · Menú principal");
        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        double alto = Math.min(770, pantalla.getHeight() - 40);
        stage.setScene(Ventanas.crearEscena(loader.load(), 1000, alto));
        stage.setMinWidth(560);
        stage.setMinHeight(480);
        Ventanas.prepararVentana(stage);
        // Al cerrar el menu principal se cierran tambien las ventanas de los modulos.
        stage.setOnHidden(e -> {
            Ventanas.cerrarTodas();
            Platform.exit();
        });
        stage.show();
    }

    @Override
    public void stop() {
        JPAUtil.cerrar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
