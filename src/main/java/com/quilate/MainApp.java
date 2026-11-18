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

        stage.setTitle("Joyería Quilate · Menú principal");
        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        double alto = Math.min(880, pantalla.getHeight() - 40);
        stage.setScene(Ventanas.crearEscena(loader.load(), Math.min(1360, pantalla.getWidth() - 40), alto));
        stage.setMinWidth(900);
        stage.setMinHeight(650);
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
