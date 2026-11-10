package com.quilate.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JPAUtil – Singleton que administra el {@link EntityManagerFactory}.
 *
 * <p>Las credenciales de conexión se leen desde variables de entorno
 * para evitar datos sensibles en el código fuente:</p>
 * <ul>
 *   <li>{@code DB_USER}  – usuario de PostgreSQL  (obligatorio)</li>
 *   <li>{@code DB_PASS}  – contraseña              (obligatorio)</li>
 *   <li>{@code DB_HOST}  – host                    (default: localhost)</li>
 *   <li>{@code DB_PORT}  – puerto                  (default: 5432)</li>
 *   <li>{@code DB_NAME}  – nombre de la BD         (obligatorio)</li>
 * </ul>
 *
 * <p><b>Uso:</b></p>
 * <pre>{@code
 *   EntityManager em = JPAUtil.getEntityManager();
 *   try {
 *       // ... operaciones ...
 *   } finally {
 *       em.close();
 *   }
 * }</pre>
 */
public final class JPAUtil {

    private static final Logger LOG = Logger.getLogger(JPAUtil.class.getName());

    /** Nombre de la unidad de persistencia definida en persistence.xml */
    private static final String PERSISTENCE_UNIT = "quilate-pu";

    /** Instancia única del factory (inicialización lazy thread-safe). */
    private static volatile EntityManagerFactory emf;

    // Constructor privado: no se debe instanciar esta clase.
    private JPAUtil() {}

    // ──────────────────────────────────────────────────────────────────────
    //  Inicialización
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Devuelve (o crea si es la primera vez) el {@link EntityManagerFactory}.
     * Usa double-checked locking para ser seguro en entornos concurrentes.
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (JPAUtil.class) {
                if (emf == null) {
                    emf = buildFactory();
                }
            }
        }
        return emf;
    }

    /**
     * Atajo para obtener un {@code EntityManager} listo para usar.
     * El llamador es responsable de cerrarlo.
     */
    public static jakarta.persistence.EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Construcción del factory leyendo variables de entorno
    // ──────────────────────────────────────────────────────────────────────

    private static EntityManagerFactory buildFactory() {
        // Leer variables de entorno
        String user   = getEnvRequired("DB_USER");
        String pass   = getEnvRequired("DB_PASS");
        String host   = getEnvOrDefault("DB_HOST", "localhost");
        String port   = getEnvOrDefault("DB_PORT", "5432");
        String dbName = getEnvRequired("DB_NAME");

        // Construir JDBC URL
        String jdbcUrl = String.format(
                "jdbc:postgresql://%s:%s/%s", host, port, dbName);

        LOG.info("Conectando a: " + jdbcUrl + " como usuario: " + user);

        // Propiedades que sobreescriben las del persistence.xml en runtime
        Map<String, String> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.url",      jdbcUrl);
        props.put("jakarta.persistence.jdbc.user",     user);
        props.put("jakarta.persistence.jdbc.password", pass);
        props.put("jakarta.persistence.jdbc.driver",   "org.postgresql.Driver");

        try {
            return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, props);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "No se pudo inicializar el EntityManagerFactory. " +
                    "Verifica las variables de entorno y que PostgreSQL esté en ejecución.", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Helpers para variables de entorno
    // ──────────────────────────────────────────────────────────────────────

    private static String getEnvRequired(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Variable de entorno requerida no encontrada: " + key +
                    "\nDefínela antes de arrancar la aplicación.");
        }
        return value;
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Cierre (llamar al cerrar la aplicación)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Cierra el {@link EntityManagerFactory}.
     * Llamar desde el método {@code stop()} de la aplicación JavaFX.
     */
    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            LOG.info("EntityManagerFactory cerrado correctamente.");
        }
    }
}
