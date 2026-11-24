package com.quilate.util;

import jakarta.persistence.EntityManager;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import org.hibernate.Session;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compila, llena y muestra los reportes JasperReports de la carpeta {@code /reportes}.
 *
 * <p>La conexión JDBC se toma de Hibernate (la misma configuración de {@link JPAUtil}),
 * así que no hay credenciales en los reportes ni en este código. El llenado corre fuera
 * del hilo de JavaFX y el visor se abre en el hilo de Swing.</p>
 */
public final class ReporteUtil {

    /** Formato de números y fechas de los reportes: C$ 1,500.00 y dd/MM/yyyy. */
    public static final Locale LOCALE = Locale.forLanguageTag("es-NI");

    private static final Map<String, JasperReport> COMPILADOS = new ConcurrentHashMap<>();

    private ReporteUtil() {
    }

    /**
     * Llena el reporte en segundo plano y lo abre en una ventana aparte.
     * Si la consulta no devuelve filas, avisa en lugar de abrir un reporte vacío.
     *
     * @param rutaJrxml  ruta en el classpath, por ejemplo {@code /reportes/inventario_general.jrxml}
     * @param parametros valores de los parámetros {@code $P{...}} del reporte
     * @param titulo     título de la ventana del visor
     */
    public static void mostrarReporte(String rutaJrxml, Map<String, Object> parametros, String titulo) {
        // Los reportes salen en la misma moneda que el usuario eligió para las pantallas.
        Map<String, Object> conMoneda = new HashMap<>(parametros);
        conMoneda.putAll(parametrosMoneda(com.quilate.ui.MonedaVista.enDolares(), com.quilate.ui.MonedaVista.tasa()));
        Tareas.ejecutar(() -> llenar(rutaJrxml, conMoneda), impreso -> {
            if (impreso.getPages().isEmpty()) {
                AlertUtil.info("El reporte «" + titulo + "» no tiene datos para mostrar.");
                return;
            }
            SwingUtilities.invokeLater(() -> {
                // false: cerrar el visor no debe cerrar la aplicación JavaFX.
                JasperViewer visor = new JasperViewer(impreso, false);
                visor.setTitle(titulo + " · Joyería Quilate");
                // Mismo tamaño y posición en Windows y macOS: centrado y sin salirse de la pantalla.
                java.awt.Rectangle area = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getMaximumWindowBounds();
                visor.setSize(Math.min(1100, area.width - 40), Math.min(820, area.height - 40));
                visor.setLocationRelativeTo(null);
                visor.setVisible(true);
                visor.toFront();
            });
        }, Tareas::mostrarError);
    }

    /** Llena el reporte con la conexión de Hibernate. Público para poder probarlo y exportarlo. */
    public static JasperPrint llenar(String rutaJrxml, Map<String, Object> parametros) {
        JasperReport reporte = compilar(rutaJrxml);
        Map<String, Object> valores = new HashMap<>(parametros);
        valores.putIfAbsent(JRParameter.REPORT_LOCALE, LOCALE);

        EntityManager em = JPAUtil.getEntityManager();
        try {
            // La conexión solo es válida dentro de doReturningWork: el reporte se llena aquí.
            return em.unwrap(Session.class).doReturningWork(conexion -> {
                try {
                    return JasperFillManager.fillReport(reporte, valores, conexion);
                } catch (JRException e) {
                    throw new IllegalStateException("No se pudo generar el reporte: " + e.getMessage(), e);
                }
            });
        } finally {
            em.close();
        }
    }

    /**
     * Parámetros de un rango para columnas fecha-hora (ventas, apartados):
     * {@code p_desde} incluido, {@code p_hasta} excluido (el día siguiente al último)
     * y {@code p_periodo} con el texto que se imprime. Sin fechas = todo el historial.
     */
    public static Map<String, Object> rangoFechaHora(LocalDate desde, LocalDate hasta) {
        LocalDate inicio = desde == null ? LocalDate.of(1900, 1, 1) : desde;
        LocalDate fin = hasta == null ? LocalDate.of(2999, 12, 31) : hasta;
        validarRango(inicio, fin);
        Map<String, Object> valores = new HashMap<>();
        valores.put("p_desde", Timestamp.valueOf(inicio.atStartOfDay()));
        valores.put("p_hasta", Timestamp.valueOf(fin.plusDays(1).atStartOfDay()));
        valores.put("p_periodo", textoPeriodo(desde, hasta));
        return valores;
    }

    /** Igual que {@link #rangoFechaHora}, para columnas DATE (pedidos): ambos extremos incluidos. */
    public static Map<String, Object> rangoFecha(LocalDate desde, LocalDate hasta) {
        LocalDate inicio = desde == null ? LocalDate.of(1900, 1, 1) : desde;
        LocalDate fin = hasta == null ? LocalDate.of(2999, 12, 31) : hasta;
        validarRango(inicio, fin);
        Map<String, Object> valores = new HashMap<>();
        valores.put("p_fecha_desde", Date.valueOf(inicio));
        valores.put("p_fecha_hasta", Date.valueOf(fin));
        valores.put("p_periodo", textoPeriodo(desde, hasta));
        return valores;
    }

    private static void validarRango(LocalDate inicio, LocalDate fin) {
        if (inicio.isAfter(fin)) {
            throw new com.quilate.exception.ReglaNegocioException(
                    "La fecha inicial no puede ser posterior a la final.");
        }
    }

    private static String textoPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde == null && hasta == null) return "Todo el historial";
        if (desde == null) return "Hasta el " + Formatos.fecha(hasta);
        if (hasta == null) return "Desde el " + Formatos.fecha(desde);
        return "Del " + Formatos.fecha(desde) + " al " + Formatos.fecha(hasta);
    }

    /**
     * Parámetros de moneda de los reportes. Los importes se guardan en córdobas;
     * en dólares se dividen entre la tasa (córdobas por US$ 1).
     */
    public static Map<String, Object> parametrosMoneda(boolean dolares, BigDecimal tasa) {
        Map<String, Object> valores = new HashMap<>();
        valores.put("p_simbolo", dolares ? "US$" : "C$");
        valores.put("p_tasa", dolares ? tasa : BigDecimal.ONE);
        return valores;
    }

    /** Convierte un importe en córdobas a la moneda del reporte; lo usan los .jrxml. */
    public static BigDecimal monto(Object cordobas, BigDecimal tasa) {
        if (cordobas == null) return null;
        BigDecimal valor = cordobas instanceof BigDecimal b ? b : new BigDecimal(cordobas.toString());
        BigDecimal divisor = tasa == null || tasa.signum() <= 0 ? BigDecimal.ONE : tasa;
        return valor.divide(divisor, 2, java.math.RoundingMode.HALF_UP);
    }

    /** Compila el .jrxml una sola vez por ejecución y reutiliza el resultado. */
    public static JasperReport compilar(String rutaJrxml) {
        return COMPILADOS.computeIfAbsent(rutaJrxml, ruta -> {
            try (InputStream entrada = ReporteUtil.class.getResourceAsStream(ruta)) {
                if (entrada == null) {
                    throw new IllegalArgumentException("No existe el reporte " + ruta + ".");
                }
                return JasperCompileManager.compileReport(entrada);
            } catch (JRException | IOException e) {
                throw new IllegalStateException("El reporte " + ruta + " tiene errores: " + e.getMessage(), e);
            }
        });
    }
}
