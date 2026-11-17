package com.quilate;

import com.quilate.model.Categoria;
import com.quilate.model.Pieza;
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
}
