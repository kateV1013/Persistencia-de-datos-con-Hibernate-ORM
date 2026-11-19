package com.quilate;

import com.quilate.model.*;
import com.quilate.util.JPAUtil;
import com.quilate.util.ReporteUtil;
import com.quilate.util.Transaccion;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprueba los reportes sin abrir el visor ni exportar PDF:
 * que todos los .jrxml compilen y que las consultas devuelvan lo esperado.
 */
class ReportesTest {

    @AfterEach
    void cerrar() {
        JPAUtil.cerrar();
    }

    @Test
    void todosLosReportesCompilan() throws Exception {
        List<String> rutas = reportes();
        assertFalse(rutas.isEmpty(), "No se encontraron reportes en /reportes");
        for (String ruta : rutas) {
            assertNotNull(ReporteUtil.compilar(ruta), ruta);
        }
    }

    @Test
    void reportesDeInventarioDevuelvenLosDatosEsperados() {
        BaseDatosPrueba.iniciar("reportes_inventario_" + System.nanoTime());
        Long[] ids = Transaccion.ejecutar(em -> {
            Categoria anillos = new Categoria("Anillos", "Anillos y argollas de oro");
            Categoria cadenas = new Categoria("Cadenas", null);
            em.persist(anillos);
            em.persist(cadenas);
            Pieza solitario = new Pieza("INV-001", "Anillo solitario", 900.0, 1800.0, 2, anillos);
            solitario.setMaterial("Oro");
            solitario.setQuilates((short) 18);
            solitario.setPesoGramos(new BigDecimal("3.50"));
            em.persist(solitario);
            em.persist(new Pieza("INV-002", "Argolla", 500.0, 1000.0, 10, anillos));
            em.persist(new Pieza("INV-003", "Cadena cubana", 1200.0, 2000.0, 1, cadenas));
            em.flush();
            return new Long[]{anillos.getId(), solitario.getId()};
        });

        JasperPrint general = ReporteUtil.llenar("/reportes/inventario_general.jrxml", Map.of());
        List<String> textoGeneral = textos(general);
        assertTrue(textoGeneral.contains("JOYERÍA QUILATE - MANAGUA"));
        assertTrue(textoGeneral.contains("Anillos"));
        assertTrue(textoGeneral.contains("Cadenas"));
        // Total: 2*1800 + 10*1000 + 1*2000 = 15,600 en 13 unidades.
        assertTrue(textoGeneral.contains("C$ 15,600.00"), textoGeneral.toString());
        assertTrue(textoGeneral.contains("13"));

        List<String> porCategoria = textos(ReporteUtil.llenar("/reportes/inventario_por_categoria.jrxml",
                Map.of("p_categoria_id", ids[0])));
        assertTrue(porCategoria.contains("Piezas de la categoría Anillos"));
        assertFalse(porCategoria.contains("Cadena cubana"));
        assertTrue(porCategoria.contains("C$ 13,600.00"), porCategoria.toString());

        List<String> stockBajo = textos(ReporteUtil.llenar("/reportes/inventario_stock_bajo.jrxml",
                Map.of("p_stock_minimo", 2)));
        assertTrue(stockBajo.contains("Cadena cubana"));
        assertTrue(stockBajo.contains("Anillo solitario"));
        assertFalse(stockBajo.contains("Argolla"));
        assertTrue(stockBajo.contains("2 pieza(s) por reponer"));

        List<String> ficha = textos(ReporteUtil.llenar("/reportes/inventario_ficha_pieza.jrxml",
                Map.of("p_pieza_id", ids[1])));
        assertTrue(ficha.contains("Ficha de pieza · Anillo solitario"));
        assertTrue(ficha.contains("18 K"));
        assertTrue(ficha.contains("3.50 g"));
        assertTrue(ficha.contains("Anillos y argollas de oro"));
        assertTrue(ficha.contains("C$ 3,600.00"), ficha.toString());

        // Sin filas: el reporte no tiene páginas y ReporteUtil avisa en lugar de abrirlo.
        assertTrue(ReporteUtil.llenar("/reportes/inventario_stock_bajo.jrxml",
                Map.of("p_stock_minimo", 0)).getPages().isEmpty());
    }

    /** Datos de los bloques 2 a 4: ventas, apartados, pedidos y servicios. */
    private record Ids(Long ana, Long venta1, Long apartadoActivo, Integer pedido1, Integer grabado) { }

    private Ids cargarMovimientos() {
        BaseDatosPrueba.iniciar("reportes_movimientos_" + System.nanoTime());
        return Transaccion.ejecutar(em -> {
            Cliente ana = new Cliente("Ana", "Lopez", "88880000", "ana@example.com");
            Cliente luis = new Cliente("Luis", null, "88881111", null);
            em.persist(ana);
            em.persist(luis);
            Categoria categoria = new Categoria("Anillos", null);
            em.persist(categoria);
            Pieza a = new Pieza("A-1", "Argolla", 60.0, 100.0, 10, categoria);
            Pieza b = new Pieza("B-1", "Cadena", 200.0, 300.0, 5, categoria);
            em.persist(a);
            em.persist(b);

            Venta v1 = new Venta(ana, LocalDateTime.of(2026, 11, 10, 10, 0), "TARJETA", EstadoVenta.CONFIRMADA);
            v1.agregarDetalle(new DetalleVenta(a, 2, new BigDecimal("100.00"), new BigDecimal("60.00"), new BigDecimal("10.00")));
            v1.agregarDetalle(new DetalleVenta(b, 1, new BigDecimal("300.00"), new BigDecimal("200.00"), BigDecimal.ZERO));
            v1.setTotal(new BigDecimal("480.00"));
            Venta v2 = new Venta(luis, LocalDateTime.of(2026, 11, 12, 16, 30), "EFECTIVO", EstadoVenta.CONFIRMADA);
            v2.agregarDetalle(new DetalleVenta(a, 1, new BigDecimal("100.00"), new BigDecimal("60.00"), BigDecimal.ZERO));
            v2.setTotal(new BigDecimal("100.00"));
            Venta anulada = new Venta(ana, LocalDateTime.of(2026, 11, 11, 9, 0), "EFECTIVO", EstadoVenta.ANULADA);
            anulada.agregarDetalle(new DetalleVenta(b, 1, new BigDecimal("300.00"), new BigDecimal("200.00"), BigDecimal.ZERO));
            anulada.setTotal(new BigDecimal("300.00"));
            em.persist(v1);
            em.persist(v2);
            em.persist(anulada);

            Apartado activo = new Apartado(ana, b, 1);
            activo.setFecha(LocalDateTime.of(2026, 11, 1, 11, 0));
            Apartado cancelado = new Apartado(luis, a, 1);
            cancelado.setFecha(LocalDateTime.of(2026, 11, 5, 11, 0));
            cancelado.setEstado(EstadoApartado.CANCELADO);
            em.persist(activo);
            em.persist(cancelado);

            TipoServicio grabado = new TipoServicio("Grabado", "Iniciales", new BigDecimal("300.00"));
            TipoServicio limpieza = new TipoServicio("Limpieza", null, new BigDecimal("250.00"));
            em.persist(grabado);
            em.persist(limpieza);
            PedidoPersonalizado p1 = new PedidoPersonalizado(ana, grabado, "Grabar iniciales", LocalDate.of(2026, 11, 16), new BigDecimal("375.00"));
            DisenoPieza diseno = new DisenoPieza();
            diseno.setTexto("V. M.");
            p1.setDiseno(diseno);
            PedidoPersonalizado p2 = new PedidoPersonalizado(luis, limpieza, "Limpiar pulsera", LocalDate.of(2026, 11, 18), new BigDecimal("250.00"));
            p2.setEstado(EstadoPedido.EN_PROCESO);
            PedidoPersonalizado p3 = new PedidoPersonalizado(ana, grabado, "Grabar fecha", LocalDate.of(2026, 11, 17), new BigDecimal("300.00"));
            p3.setEstado(EstadoPedido.CANCELADO);
            em.persist(p1);
            em.persist(p2);
            em.persist(p3);
            em.flush();
            return new Ids(ana.getId(), v1.getId(), activo.getId(), p1.getId(), grabado.getId());
        });
    }

    private static List<String> llenar(String reporte, Map<String, Object> parametros) {
        return textos(ReporteUtil.llenar("/reportes/" + reporte + ".jrxml", parametros));
    }

    private static void contiene(List<String> textos, String... esperados) {
        for (String esperado : esperados) {
            assertTrue(textos.contains(esperado), "Falta «" + esperado + "» en " + textos);
        }
    }

    @Test
    void reportesDeVentasYAnalisis() {
        Ids ids = cargarMovimientos();
        Map<String, Object> noviembre = ReporteUtil.rangoFechaHora(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

        contiene(llenar("ventas_por_fechas", noviembre),
                "Del 01/11/2026 al 30/11/2026 · Emitido el " + textoEmision(), "2 venta(s) confirmada(s)", "C$ 580.00", "Tarjeta");
        contiene(llenar("ventas_comprobante", Map.of("p_venta_id", ids.venta1())),
                "Comprobante de venta N.º " + ids.venta1(), "Ana Lopez", "Confirmada", "C$ 480.00");
        contiene(llenar("ventas_por_estado", noviembre),
                "Por estado", "Por método de pago (solo confirmadas)", "Anulada", "C$ 300.00", "Efectivo");
        // Ingreso 580, costo 380 (3 × 60 + 200), ganancia 200, margen 34.48 %, descuentos 20.
        contiene(llenar("analisis_resumen", noviembre), "2", "4", "C$ 580.00", "C$ 380.00", "C$ 200.00", "34.48 %", "C$ 20.00");
        List<String> masVendidas = llenar("analisis_piezas_mas_vendidas", noviembre);
        assertTrue(masVendidas.indexOf("Argolla") < masVendidas.indexOf("Cadena"), masVendidas.toString());
        contiene(llenar("analisis_rentabilidad", noviembre), "35.71 %", "33.33 %", "34.48 %");
        // Rango sin ventas: sin páginas, ReporteUtil avisa en lugar de abrir el visor.
        assertTrue(ReporteUtil.llenar("/reportes/analisis_resumen.jrxml",
                ReporteUtil.rangoFechaHora(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 31))).getPages().isEmpty());
    }

    @Test
    void reportesDeClientesYApartados() {
        Ids ids = cargarMovimientos();
        Map<String, Object> noviembre = ReporteUtil.rangoFechaHora(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

        contiene(llenar("clientes_directorio", Map.of()), "Ana Lopez", "Luis", "2 cliente(s)");
        List<String> historial = llenar("clientes_historial", Map.of("p_cliente_id", ids.ana()));
        contiene(historial, "Historial de compras · Ana Lopez", "C$ 480.00");
        assertEquals(2, historial.stream().filter(t -> t.equals("Argolla") || t.equals("Cadena")).count(),
                "La venta anulada no debe aparecer: " + historial);
        List<String> mejores = llenar("clientes_mejores", noviembre);
        assertTrue(mejores.indexOf("Ana Lopez") < mejores.indexOf("Luis"), mejores.toString());
        contiene(llenar("apartados_activos", Map.of()), "1 apartado(s) activo(s) · valor reservado", "C$ 300.00");
        contiene(llenar("apartados_comprobante", Map.of("p_apartado_id", ids.apartadoActivo())),
                "Comprobante de apartado N.º " + ids.apartadoActivo(), "Activo", "Cadena");
        contiene(llenar("apartados_por_estado", noviembre), "Activo", "Cancelado", "Total: 2 apartado(s) en el período");
    }

    @Test
    void reportesDelTaller() {
        Ids ids = cargarMovimientos();
        contiene(llenar("pedidos_agenda_semanal", ReporteUtil.rangoFecha(LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22))),
                "2 de 20 pedidos de la semana", "lunes 16/11/2026", "En proceso");
        contiene(llenar("pedidos_orden_trabajo", Map.of("p_pedido_id", ids.pedido1())),
                "Orden de trabajo N.º " + ids.pedido1(), "«V. M.»", "Anillo · Oro · fuente Clásica", "18.00 × 5.00 mm · letra 2.00 mm");
        contiene(llenar("pedidos_por_estado", ReporteUtil.rangoFecha(null, null)),
                "Todo el historial · Emitido el " + textoEmision(), "Cancelado", "Pendiente", "En proceso",
                "C$ 300.00", "Total sin cancelados", "C$ 625.00");
        contiene(llenar("servicios_catalogo", Map.of()), "Grabado", "Limpieza", "2 servicio(s)");
        contiene(llenar("servicios_ingresos", ReporteUtil.rangoFecha(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30))),
                "C$ 375.00", "C$ 250.00", "C$ 625.00", "Entregado (cobrado)", "Por entregar");
        contiene(llenar("servicios_pedidos", Map.of("p_servicio_id", ids.grabado())),
                "Pedidos del servicio Grabado", "2 pedido(s)", "C$ 675.00");
    }

    private static String textoEmision() {
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());
    }

    private static List<String> reportes() throws URISyntaxException, java.io.IOException {
        Path carpeta = Path.of(ReportesTest.class.getResource("/reportes").toURI());
        try (Stream<Path> archivos = Files.list(carpeta)) {
            return archivos.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".jrxml"))
                    .sorted()
                    .map(n -> "/reportes/" + n)
                    .toList();
        }
    }

    private static List<String> textos(JasperPrint impreso) {
        return impreso.getPages().stream()
                .flatMap(pagina -> pagina.getElements().stream())
                .flatMap(ReportesTest::aplanar)
                .filter(JRPrintText.class::isInstance)
                .map(e -> ((JRPrintText) e).getFullText())
                .filter(t -> t != null)
                .toList();
    }

    private static Stream<JRPrintElement> aplanar(JRPrintElement elemento) {
        if (elemento instanceof net.sf.jasperreports.engine.JRPrintFrame marco) {
            return Stream.concat(Stream.of(elemento), marco.getElements().stream().flatMap(ReportesTest::aplanar));
        }
        return Stream.of(elemento);
    }

    @Test
    void losReportesSalenEnLaMonedaElegida() {
        cargarMovimientos();
        Map<String, Object> noviembre = ReporteUtil.rangoFechaHora(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        BigDecimal tasa = new BigDecimal("36.6243");

        // Córdobas (por defecto): igual que siempre.
        List<String> enCordobas = llenar("ventas_por_fechas", noviembre);
        contiene(enCordobas, "C$ 580.00", "Importes en córdobas (C$)");

        // Dólares: C$ 580.00 / 36.6243 = US$ 15.84, y el pie indica la tasa usada.
        Map<String, Object> enDolares = new java.util.HashMap<>(noviembre);
        enDolares.putAll(ReporteUtil.parametrosMoneda(true, tasa));
        List<String> textos = llenar("ventas_por_fechas", enDolares);
        contiene(textos, "US$ 15.84", "Importes en dólares · tasa C$ 36.6243 por US$ 1");
        assertTrue(textos.stream().noneMatch(t -> t.startsWith("C$ ")), "Quedó un importe en córdobas: " + textos);

        // Un reporte de ficha y uno de totales también convierten.
        Map<String, Object> resumen = new java.util.HashMap<>(noviembre);
        resumen.putAll(ReporteUtil.parametrosMoneda(true, tasa));
        contiene(llenar("analisis_resumen", resumen), "US$ 15.84", "US$ 10.38", "US$ 5.46", "34.48 %");
    }
}
