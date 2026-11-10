package com.quilate.controller;

import com.quilate.util.JPAUtil;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Ventana principal. Cada integrante agrega su pestana en main.fxml
 * con fx:include apuntando a su propio FXML (piezas.fxml, ventas.fxml, ...).
 */
public class MainController {

    @FXML
    private Label lblEstado;

    @FXML
    private void initialize() {
        lblEstado.setText("Conectando a PostgreSQL...");

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
        conexion.setOnSucceeded(e -> lblEstado.setText("Conectado a la base de datos"));
        conexion.setOnFailed(e -> {
            Throwable err = conexion.getException();
            while (err.getCause() != null) {
                err = err.getCause();
            }
            lblEstado.setText("Sin conexion: " + err.getMessage());
        });

        Thread hilo = new Thread(conexion, "conexion-bd");
        hilo.setDaemon(true);
        hilo.start();
    }
}
