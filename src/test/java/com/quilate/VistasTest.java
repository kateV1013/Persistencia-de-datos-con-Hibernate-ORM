package com.quilate;

import com.quilate.ui.Modulo;
import com.quilate.util.JPAUtil;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class VistasTest {
    @Test void cargaMenuPrincipalYTodasLasVentanasDeModulos() throws Exception {
        BaseDatosPrueba.iniciar("vistas_" + System.nanoTime());
        CompletableFuture<List<Parent>> cargadas = new CompletableFuture<>();
        Runnable cargar = () -> {
            try {
                List<Parent> vistas = new ArrayList<>();
                vistas.add(FXMLLoader.load(getClass().getResource("/fxml/main.fxml")));
                for (Modulo modulo : Modulo.values()) {
                    vistas.add(FXMLLoader.load(getClass().getResource(modulo.fxml())));
                }
                cargadas.complete(vistas);
            } catch (Throwable e) { cargadas.completeExceptionally(e); }
        };
        try {
            Platform.startup(() -> { Platform.setImplicitExit(false); cargar.run(); });
        } catch (IllegalStateException iniciada) { Platform.runLater(cargar); }
        try {
            List<Parent> vistas = cargadas.get(30, TimeUnit.SECONDS);
            assertEquals(Modulo.values().length + 1, vistas.size());
            Parent menu = vistas.get(0);
            CompletableFuture<Integer> tarjetas = new CompletableFuture<>();
            // Sin escena, lookupAll no entra al contenido de un ScrollPane: se busca desde su contenido.
            Platform.runLater(() -> tarjetas.complete(((ScrollPane) menu.lookup(".menu-scroll"))
                    .getContent().lookupAll(".tarjeta-modulo").size()));
            assertEquals(Modulo.values().length, tarjetas.get(10, TimeUnit.SECONDS));
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
