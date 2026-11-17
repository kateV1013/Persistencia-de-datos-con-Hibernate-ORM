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
        Tareas.ejecutar(() -> llenar(rutaJrxml, parametros), impreso -> {
            if (impreso.getPages().isEmpty()) {
                AlertUtil.info("El reporte «" + titulo + "» no tiene datos para mostrar.");
                return;
            }
            SwingUtilities.invokeLater(() -> {
                // false: cerrar el visor no debe cerrar la aplicación JavaFX.
                JasperViewer visor = new JasperViewer(impreso, false);
                visor.setTitle(titulo + " · Joyería Quilate");
                visor.setVisible(true);
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
