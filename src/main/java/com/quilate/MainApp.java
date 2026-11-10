package com.quilate;

import com.quilate.util.JPAUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(loader.load(), 1100, 700);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());

        stage.setTitle("Quilate - Sistema de Ventas para Joyeria");
        stage.setScene(scene);
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
