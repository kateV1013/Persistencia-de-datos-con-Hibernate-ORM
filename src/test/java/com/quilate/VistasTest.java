package com.quilate;

import com.quilate.util.JPAUtil;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TabPane;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class VistasTest {
    @Test void cargaVentanaPrincipalConTodosLosModulos() throws Exception {
        BaseDatosPrueba.iniciar("vistas_" + System.nanoTime());
        CompletableFuture<Parent> cargada = new CompletableFuture<>();
        Runnable cargar = () -> {
            try {
                cargada.complete(FXMLLoader.load(getClass().getResource("/fxml/main.fxml")));
            } catch (Throwable e) { cargada.completeExceptionally(e); }
        };
        try {
            Platform.startup(() -> { Platform.setImplicitExit(false); cargar.run(); });
        } catch (IllegalStateException iniciada) { Platform.runLater(cargar); }
        try {
            Parent raiz = cargada.get(30, TimeUnit.SECONDS);
            assertNotNull(raiz);
            CompletableFuture<Integer> pestanas = new CompletableFuture<>();
            Platform.runLater(() -> pestanas.complete(((TabPane) raiz.lookup(".tab-pane")).getTabs().size()));
            assertEquals(7, pestanas.get(10, TimeUnit.SECONDS));
        } finally {
            for (Thread hilo : Thread.getAllStackTraces().keySet()) {
                if (hilo.getName().equals("quilate-bd") || hilo.getName().equals("conexion-bd")) {
                    hilo.join(10000);
                }
            }
            CompletableFuture<Void> callbacks = new CompletableFuture<>();
            Platform.runLater(() -> callbacks.complete(null));
            callbacks.get(10, TimeUnit.SECONDS);
            JPAUtil.cerrar();
        }
    }
}
