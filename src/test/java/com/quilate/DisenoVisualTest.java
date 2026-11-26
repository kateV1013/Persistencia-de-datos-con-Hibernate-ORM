package com.quilate;

import com.quilate.model.*;
import com.quilate.service.*;
import com.quilate.ui.*;
import com.quilate.util.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;

/** Verifica búsqueda, navegación y carga con CSS. Capturas opcionales: -Dquilate.capturas=true. */
class DisenoVisualTest {
    private static <T> T fx(Callable<T> tarea) throws Exception {
        CompletableFuture<T> resultado = new CompletableFuture<>();
        Platform.runLater(() -> { try { resultado.complete(tarea.call()); }
            catch (Throwable e) { resultado.completeExceptionally(e); } });
        return resultado.get(30, TimeUnit.SECONDS);
    }
    @Test void busquedaNavegacionYEstilos() throws Exception {
        BaseDatosPrueba.iniciar("diseno_" + System.nanoTime());
        try { Platform.startup(() -> Platform.setImplicitExit(false)); }
        catch (IllegalStateException yaIniciada) { /* La prueba de FXML inició JavaFX. */ }
        boolean capturar = Boolean.getBoolean("quilate.capturas");
        sembrarDatosDePrueba();
        Stage principal = fx(() -> {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
            Stage stage = new Stage();
            stage.setScene(Ventanas.crearEscena(root, 1020, 680)); stage.show();
            root.applyCss(); root.layout();
            TextField filtro = (TextField) root.lookup("#busqueda");
            filtro.setText("INVENTARIO");
            assertEquals(1, root.lookupAll(".fila-modulo").stream().filter(n -> n.isManaged()).count());
            filtro.setText("zzzzz");
            assertTrue(root.lookup("#sinResultados").isVisible());
            filtro.clear();
            assertEquals(7, root.lookupAll(".fila-modulo").stream().filter(n -> n.isManaged()).count());
            ToggleButton tienda = (ToggleButton) root.lookup("#vistaTienda");
            ToggleButton taller = (ToggleButton) root.lookup("#vistaTaller");
            tienda.fire();
            assertEquals(4, root.lookupAll(".fila-modulo").stream().filter(n -> n.isManaged()).count());
            assertFalse(root.lookup("#divisorSecciones").isManaged());
            taller.fire();
            taller.fire();
            assertTrue(taller.isSelected());
            assertEquals(3, root.lookupAll(".fila-modulo").stream().filter(n -> n.isManaged()).count());
            filtro.setText("inventario");
            assertTrue(root.lookup("#sinResultados").isVisible());
            ((ToggleButton) root.lookup("#vistaGeneral")).fire();
            assertEquals(7, root.lookupAll(".fila-modulo").stream().filter(n -> n.isManaged()).count());
            assertTrue(root.lookup("#divisorSecciones").isManaged());
            return stage;
        });
        try {
            Thread.sleep(500);
            if (capturar) fx(() -> { captura(principal, "01-inicio"); return null; });
            int numero = 2;
            for (Modulo modulo : Modulo.values()) {
                Stage ventana = fx(() -> {
                    Button fila = principal.getScene().getRoot().lookupAll(".fila-modulo").stream()
                            .filter(n -> ("Abrir " + modulo.titulo()).equals(n.getAccessibleText()))
                            .map(Button.class::cast).findFirst().orElseThrow();
                    fila.fire();
                    Stage abierta = Window.getWindows().stream().filter(w -> w instanceof Stage)
                            .map(w -> (Stage) w).filter(w -> (modulo.titulo() + " · Joyería Quilate").equals(w.getTitle()))
                            .findFirst().orElseThrow();
                    assertNotNull(abierta.getScene().getRoot());
                    // Abrir el mismo módulo no crea otra ventana.
                    int cantidad = Window.getWindows().size();
                    if (modulo == Modulo.VENTAS) {
                        ((Button) principal.getScene().lookup("#registrarVenta")).fire();
                    } else {
                        fila.fire();
                    }
                    assertEquals(cantidad, Window.getWindows().size());
                    return abierta;
                });
                Thread.sleep(500);
                if (modulo == Modulo.CLIENTES) {
                    esperar(() -> !((TableView<?>) ventana.getScene().lookup("#tabla")).getItems().isEmpty());
                    fx(() -> {
                        var tabla = (TableView<?>) ventana.getScene().lookup("#tabla");
                        var filtro = (TextField) ventana.getScene().lookup("#busqueda");
                        int total = tabla.getItems().size();
                        filtro.setText("8888-0001");
                        assertEquals(1, tabla.getItems().size());
                        assertEquals("8888-0001", tabla.getColumns().get(1).getCellData(0));
                        filtro.setText("88880001");
                        assertEquals(1, tabla.getItems().size());
                        filtro.setText("CLIENTE1@EXAMPLE.COM");
                        assertEquals(1, tabla.getItems().size());
                        filtro.clear();
                        assertEquals(total, tabla.getItems().size());
                        return null;
                    });
                    fx(() -> { ((TableView<?>)ventana.getScene().lookup("#tabla")).getSelectionModel().selectFirst(); return null; });
                    esperar(() -> !"—".equals(((Label)ventana.getScene().lookup("#lblPerfilCompras")).getText()));
                    fx(() -> { assertNotEquals("—",((Label)ventana.getScene().lookup("#lblPerfilCompras")).getText()); return null; });
                }
                if (modulo == Modulo.VENTAS) probarEscaneo(ventana);
                if (modulo == Modulo.PEDIDOS) probarEditor(ventana, capturar);
                if(modulo==Modulo.INVENTARIO) {
                    esperar(() -> !((TableView<?>)ventana.getScene().lookup("#tablaPiezas")).getItems().isEmpty());
                    fx(() -> { ((TableView<?>)ventana.getScene().lookup("#tablaPiezas")).getSelectionModel().selectFirst(); return null; });
                    Platform.runLater(() -> ventana.getScene().getRoot().lookupAll(".button").stream()
                        .filter(n -> n instanceof Button b && b.getText().equals("Ver código / etiqueta"))
                        .map(n -> (Button)n).findFirst().orElseThrow().fire());
                    esperar(() -> Window.getWindows().stream().anyMatch(w -> w instanceof Stage st && String.valueOf(st.getTitle()).startsWith("Código de barras")));
                    fx(() -> {
                        Stage etiqueta=(Stage)Window.getWindows().stream().filter(w -> w instanceof Stage st && String.valueOf(st.getTitle()).startsWith("Código de barras")).findFirst().orElseThrow();
                        if(capturar) captura(etiqueta,"13-etiqueta-barras");
                        etiqueta.close();return null;
                    });
                }

                if (capturar) {
                    String nombre = String.format("%02d-%s", numero++, modulo.name().toLowerCase(Locale.ROOT));
                    fx(() -> { captura(ventana, nombre); return null; });
                    if (modulo == Modulo.VENTAS || modulo == Modulo.PEDIDOS || modulo == Modulo.INVENTARIO) {
                        fx(() -> { ventana.setWidth(1060); ventana.setHeight(720); return null; });
                        Thread.sleep(200);
                        fx(() -> { captura(ventana, nombre + "-compacto"); return null; });
                        if (modulo == Modulo.VENTAS) {
                            fx(() -> { ((ScrollPane)ventana.getScene().lookup("#scrollVenta")).setVvalue(1); return null; });
                            Thread.sleep(150);
                            fx(() -> { captura(ventana, nombre + "-confirmacion"); return null; });
                        }
                    }
                }
                if (modulo == Modulo.REPORTE_PIEZAS) {
                    fx(() -> { assertEquals(LocalDate.now().minusDays(29),((DatePicker)ventana.getScene().lookup("#dpDesde")).getValue()); assertEquals(LocalDate.now(),((DatePicker)ventana.getScene().lookup("#dpHasta")).getValue()); return null; });
                    fx(() -> { DatePicker fecha=(DatePicker)ventana.getScene().lookup("#dpDesde"); assertEquals("15/11/2026",fecha.getConverter().toString(LocalDate.of(2026,11,15))); assertThrows(java.time.format.DateTimeParseException.class,()->fecha.getConverter().fromString("31/02/2026")); return null; });
                    esperar(() -> ventana.getScene().lookup(".ranking-relleno") != null);
                    fx(() -> {
                        var escena = ventana.getScene();
                        var menu = (MenuButton) escena.lookup(".selector-moneda");
                        var ingresos = (Label) escena.lookup("#lblIngresos");
                        assertTrue(ingresos.getText().startsWith("C$ "));
                        String cordobas = ingresos.getText();
                        var barra = escena.lookup(".ranking-relleno");
                        assertTrue(barra.getLayoutBounds().getWidth() > 0);
                        var piezas = ((TabPane) escena.lookup("#reportes")).getTabs().getFirst().getContent();
                        assertEquals(10, piezas.lookupAll(".ranking-nombre").size());
                        var lista = (javafx.scene.layout.VBox) piezas.lookup(".ranking-lista");
                        var desplazamiento = (ScrollPane) piezas.lookup(".ranking-panel .scroll-pane");
                        assertTrue(escena.getHeight() < 820 || desplazamiento.getViewportBounds().getHeight() >= lista.getHeight() - 1,
                                "El ranking completo debe caber en una ventana de escritorio");
                        try {
                            menu.getItems().get(1).fire();
                            var base = com.quilate.util.Formatos.leerDinero(cordobas, "ingresos");
                            var usd = base.divide(new BigDecimal("36.6243"), 2, java.math.RoundingMode.HALF_UP);
                            assertEquals(usd, new BigDecimal(ingresos.getText().substring(4).replace(",", "")));
                        } finally {
                            menu.getItems().get(0).fire();
                        }
                        assertEquals(cordobas, ingresos.getText());
                        return null;
                    });
                    for (int t=0;t<6;t++) {
                        int indice=t;
                        fx(() -> { TabPane tabs=(TabPane)ventana.getScene().lookup("#reportes"); assertEquals(6,tabs.getTabs().size()); tabs.getSelectionModel().select(indice); return null; });
                        Thread.sleep(100);
                        if(capturar) fx(() -> { captura(ventana,"10-reporte-"+indice); return null; });
                    }
                    fx(() -> {
                        var escena = ventana.getScene();
                        LocalDate manana = LocalDate.now().plusDays(1);
                        ((DatePicker) escena.lookup("#dpHasta")).setValue(manana);
                        ((DatePicker) escena.lookup("#dpDesde")).setValue(manana);
                        return null;
                    });
                    esperar(() -> "0".equals(((Label) ventana.getScene().lookup("#lblVentas")).getText()));
                    fx(() -> {
                        var escena = ventana.getScene();
                        ((DatePicker) escena.lookup("#dpDesde")).setValue(LocalDate.now().minusDays(29));
                        ((DatePicker) escena.lookup("#dpHasta")).setValue(LocalDate.now());
                        return null;
                    });
                    esperar(() -> Integer.parseInt(((Label) ventana.getScene().lookup("#lblVentas")).getText()) > 0);
                }
                // Esc cierra la ventana del módulo (y la quita de las abiertas).
                fx(() -> {
                    javafx.event.Event.fireEvent(ventana.getScene().getRoot(), new javafx.scene.input.KeyEvent(
                            javafx.scene.input.KeyEvent.KEY_PRESSED, "", "", javafx.scene.input.KeyCode.ESCAPE,
                            false, false, false, false));
                    assertFalse(ventana.isShowing(), modulo + " no se cerró con Esc");
                    return null;
                });
            }
            if (capturar) {
                fx(() -> { principal.setWidth(920); principal.setHeight(700); return null; });
                Thread.sleep(250);
                fx(() -> {
                    assertTrue(principal.getScene().lookup("#vistaGeneral").isVisible());
                    captura(principal, "09-inicio-compacto");
                    return null;
                });
            }
        } finally {
            fx(() -> { Ventanas.cerrarTodas(); principal.close(); return null; });
            for (Thread hilo : Thread.getAllStackTraces().keySet())
                if (hilo.getName().equals("quilate-bd") || hilo.getName().equals("conexion-bd")) hilo.join(10000);
            fx(() -> null);
            JPAUtil.cerrar();
        }
    }
    @SuppressWarnings("unchecked")
    /** El escaneo consulta la BD en segundo plano: se espera cada lectura antes de revisar la tabla. */
    private static void probarEscaneo(Stage ventana) throws Exception {
        TextField codigo=(TextField)ventana.getScene().lookup("#txtCodigoVenta");
        TableView<?> tabla=(TableView<?>)ventana.getScene().lookup("#tablaDetalles");
        Label mensaje=(Label)ventana.getScene().lookup("#lblMensaje");
        for(int i=1;i<=2;i++){
            int esperadas=i;
            fx(() -> { codigo.setText("QL-001"); codigo.fireEvent(new javafx.event.ActionEvent()); return null; });
            esperar(() -> tabla.getItems().size()==1 && Integer.valueOf(esperadas).equals(tabla.getColumns().get(1).getCellData(0)));
        }
        fx(() -> { assertEquals(0,new BigDecimal("3600").compareTo((BigDecimal)tabla.getColumns().get(4).getCellData(0))); return null; });
        fx(() -> { codigo.setText("NO-EXISTE"); codigo.fireEvent(new javafx.event.ActionEvent()); return null; });
        esperar(() -> mensaje.getText().contains("No existe"));
        fx(() -> { assertEquals(2,tabla.getColumns().get(1).getCellData(0)); codigo.clear(); return null; });
        fx(() -> {
            var escena = ventana.getScene();
            var descuento = (TextField) escena.lookup("#txtDescuento");
            var moneda = (Label) escena.lookup("#lblMonedaDescuento");
            var agregar = escena.getRoot().lookupAll(".button").stream()
                    .filter(n -> n instanceof Button b && "Agregar pieza".equals(b.getText()))
                    .map(Button.class::cast).findFirst().orElseThrow();
            var selector = com.quilate.ui.MonedaVista.selector();
            try {
                tabla.getSelectionModel().selectFirst();
                descuento.setText("10.00");
                selector.getItems().get(1).fire();
                assertTrue(moneda.getText().contains("US$"));
                assertEquals("0.27", descuento.getText());
                agregar.fire();
                assertEquals(0, new BigDecimal("10.00").compareTo(
                        (BigDecimal) tabla.getColumns().get(3).getCellData(0)));
                assertTrue(((Label) escena.lookup("#lblAhorro")).getText().contains("US$"));
                tabla.getSelectionModel().clearAndSelect(0);
                selector.getItems().getFirst().fire();
                assertEquals("10.00", descuento.getText());
            } finally {
                selector.getItems().getFirst().fire();
            }
            return null;
        });
    }
    @SuppressWarnings("unchecked")
    private static void esperar(Callable<Boolean> condicion) throws Exception {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (!fx(condicion)) {
            if (System.nanoTime() > limite) fail("La interfaz no alcanzó el estado esperado.");
            Thread.sleep(50);
        }
    }
    private static void probarEditor(Stage ventana, boolean capturar) throws Exception {
        esperar(() -> !((TableView<?>)ventana.getScene().lookup("#tabla")).getItems().isEmpty());
        fx(() -> { ((TableView<?>)ventana.getScene().lookup("#tabla")).getSelectionModel().selectFirst(); return null; });
        esperar(() -> !ventana.getScene().lookup("#btnDisenar").isDisabled());
        fx(() -> { assertEquals("Sin diseño guardado",ventana.getScene().lookup("#vistaGrabado").getAccessibleText()); return null; });
        fx(() -> {
            ((TableView<?>)ventana.getScene().lookup("#tabla")).getSelectionModel().select(1);
            assertTrue(ventana.getScene().lookup("#vistaGrabado").getAccessibleText().contains("D.S."));
            assertTrue(((Label)ventana.getScene().lookup("#lblDiseno")).getText().contains("Pedido N.º 2"));
            ((TableView<?>)ventana.getScene().lookup("#tabla")).getSelectionModel().selectFirst();
            assertEquals("Sin diseño guardado",ventana.getScene().lookup("#vistaGrabado").getAccessibleText());
            return null;
        });


        Platform.runLater(() -> ((Button)ventana.getScene().lookup("#btnDisenar")).fire());
        esperar(() -> Window.getWindows().stream().anyMatch(w -> w instanceof Stage st && String.valueOf(st.getTitle()).contains("Atelier Joyería Quilate")));
        fx(() -> {
            Stage modal=(Stage)Window.getWindows().stream().filter(w -> w instanceof Stage st && String.valueOf(st.getTitle()).contains("Atelier Joyería Quilate")).findFirst().orElseThrow();
            DialogPane pane=(DialogPane)modal.getScene().getRoot();
            try {
                ((TextField)pane.lookup("#textoGrabado")).setText("M & L · Siempre");
                ((ComboBox<String>)pane.lookup("#acabadoGrabado")).setValue("Oro rosa");
                ((ComboBox<String>)pane.lookup("#formaGrabado")).setValue("Placa");
                assertTrue(((Spinner<Double>)pane.lookup("#anchoGrabado")).getValue()>=8);
                if(capturar) captura(modal,"11-atelier-placa");
                for(String variante:List.of("Placa ovalada","Placa circular","Placa corazón")) {
                    ((ComboBox<String>)pane.lookup("#formaGrabado")).setValue(variante);
                    assertFalse(pane.lookupButton(pane.getButtonTypes().stream().filter(b -> b.getButtonData()==ButtonBar.ButtonData.OK_DONE).findFirst().orElseThrow()).isDisabled());
                    if("Placa circular".equals(variante)) {
                        assertEquals(((Spinner<Double>)pane.lookup("#medidaGrabado")).getValue(),((Spinner<Double>)pane.lookup("#anchoGrabado")).getValue());
                        assertTrue(pane.lookup("#anchoGrabado").isDisabled());
                    }
                    if(capturar) captura(modal,"11-"+variante.replace(' ','-'));
                }

                ((ComboBox<String>)pane.lookup("#formaGrabado")).setValue("Anillo");
                ((Spinner<Double>)pane.lookup("#anchoGrabado")).getValueFactory().setValue(5.0);
                ((Spinner<Double>)pane.lookup("#letraGrabado")).getValueFactory().setValue(2.5);
                if(capturar) captura(modal,"12-atelier-anillo");
                ButtonType aplicar=pane.getButtonTypes().stream().filter(b -> b.getButtonData()==ButtonBar.ButtonData.OK_DONE).findFirst().orElseThrow();
                assertFalse(pane.lookupButton(aplicar).isDisabled());
                ((Button)pane.lookupButton(aplicar)).fire();
            } finally { modal.close(); }
            return null;
        });
        fx(() -> { assertTrue(((Label)ventana.getScene().lookup("#lblDiseno")).getText().contains("M & L")); return null; });
    }
    private static void captura(Stage stage, String nombre) throws Exception {
        stage.getScene().getRoot().applyCss(); stage.getScene().getRoot().layout();
        WritableImage imagen = stage.getScene().snapshot(null);
        BufferedImage png = new BufferedImage((int)imagen.getWidth(), (int)imagen.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<png.getHeight();y++) for(int x=0;x<png.getWidth();x++) png.setRGB(x,y,imagen.getPixelReader().getArgb(x,y));
        Path salida = Path.of("target", "diseno"); Files.createDirectories(salida);
        ImageIO.write(png, "png", salida.resolve(nombre + ".png").toFile());
    }
    private static void sembrarDatosDePrueba() {
        // Datos ficticios únicamente en H2; nunca modifica PostgreSQL.
        Transaccion.ejecutar(em -> {
            String[] nombres = {"Anillo Aurora 18K", "Collar Esencia", "Aretes Luz", "Pulsera Infinito", "Dije Estrella", "Anillo Serenidad"};
            String[] clientes = {"María", "Ana", "Camila", "Daniel", "Lucía", "Gabriel"};
            String[] apellidos = {"López", "Martínez", "Mendoza", "García", "Reyes", "Castillo"};
            Categoria categoria = new Categoria("Colección Esencia", "Piezas de la colección"); em.persist(categoria);
            for(int i=0;i<nombres.length;i++) {
                em.persist(new Pieza("QL-00"+(i+1), nombres[i], 900.0+i*200, 1800.0+i*350, 8+i*2, categoria));
                em.persist(new Cliente(clientes[i], apellidos[i], "8888000"+i, "cliente"+i+"@example.com"));
            }
            String[] modelos = {"Argolla Horizonte - Unisex / talla 6",
                    "Pulsera Estrella - Mujer / 17 cm", "Anillo Sello - Hombre / talla 8",
                    "Dije Cruz - Hombre / 18 mm", "Collar Brisa - Mujer / 45 cm",
                    "Aretes Destello - Mujer / medianos"};
            for (int i = 0; i < modelos.length; i++) {
                em.persist(new Pieza("QL-10" + i, modelos[i], 850.0, 1700.0, 10, categoria));
            }
            em.persist(new TipoServicio("Grabado personalizado", "Iniciales y fechas especiales", new BigDecimal("300")));
            em.persist(new TipoServicio("Ajuste de talla", "Ajuste de anillos", new BigDecimal("450")));
            em.persist(new TipoServicio("Limpieza y pulido", "Cuidado y restauración", new BigDecimal("250")));
            return null;
        });
        VentaService ventas = new VentaService();
        for(long i=1;i<=5;i++) {
            Venta venta = ventas.crearEnPreparacion(i, List.of(new SolicitudDetalleVenta(i, (int)(i%3)+1, new BigDecimal(i%2==0?"150":"0"))));
            if (i<5) {
                ventas.confirmar(venta.getId());
                int dias=(int)i; Transaccion.ejecutar(em -> { em.find(Venta.class,venta.getId()).setFechaHora(LocalDate.now().minusDays(dias).atTime(dias%2==0?16:10,15)); return null; });
            }
        }
        var detallesRanking = java.util.stream.LongStream.rangeClosed(7, 12)
                .mapToObj(id -> new SolicitudDetalleVenta(id, (int) (id % 4) + 1, BigDecimal.ZERO)).toList();
        Venta ventaRanking = ventas.crearEnPreparacion(1L, detallesRanking);
        ventas.confirmar(ventaRanking.getId());
        new com.quilate.dao.ApartadoDAO().crear(2L, 6L, 1);
        Cliente cliente = new com.quilate.dao.ClienteDAO().listar().get(0);
        TipoServicio servicio = new TipoServicioService().listar().stream().filter(t -> t.getNombre().contains("Grabado")).findFirst().orElseThrow();
        new PedidoService().registrar(new PedidoPersonalizado(cliente, servicio, "Grabar iniciales M & L", LocalDate.now().plusDays(3), new BigDecimal("300")));
        Cliente otroCliente=new com.quilate.dao.ClienteDAO().listar().stream().filter(c -> !c.getId().equals(cliente.getId())).findFirst().orElseThrow();
        PedidoPersonalizado otro=new PedidoPersonalizado(otroCliente,servicio,"Grabar D.S. en placa ovalada",LocalDate.now().plusDays(2),new BigDecimal("300"));
        DisenoPieza ovalada=new DisenoPieza();ovalada.setForma("Placa ovalada");ovalada.setAcabado("Plata");ovalada.setTexto("D.S.");ovalada.setMedida(new BigDecimal("30"));ovalada.setAncho(new BigDecimal("18"));
        otro.setDiseno(ovalada);new PedidoService().registrar(otro);

    }
}
