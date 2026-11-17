package com.quilate.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class JPAUtil {

    private static final String PERSISTENCE_UNIT = "quilatePU";
    private static volatile EntityManagerFactory emf;

    private JPAUtil() {
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        EntityManagerFactory actual = emf;
        if (actual == null || !actual.isOpen()) {
            synchronized (JPAUtil.class) {
                actual = emf;
                if (actual == null || !actual.isOpen()) {
                    emf = actual = Persistence.createEntityManagerFactory(
                            PERSISTENCE_UNIT, cargarPropiedades());
                }
            }
        }
        return actual;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /** Permite que las pruebas usen H2 sin depender de credenciales locales. */
    public static synchronized void inicializar(Map<String, Object> propiedades) {
        cerrar();
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, propiedades);
    }

    public static synchronized void cerrar() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }

    public static void shutdown() {
        cerrar();
    }

    private static Map<String, Object> cargarPropiedades() {
        Properties props = new Properties();
        Path archivo = Path.of("db.properties");
        if (Files.exists(archivo)) {
            try (InputStream in = Files.newInputStream(archivo)) {
                props.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo leer db.properties.", e);
            }
        }

        copiarEntorno(props, "DB_URL", "jakarta.persistence.jdbc.url");
        copiarEntorno(props, "DB_USER", "jakarta.persistence.jdbc.user");
        copiarEntorno(props, "DB_PASSWORD", "jakarta.persistence.jdbc.password");
        copiarEntorno(props, "DB_PASS", "jakarta.persistence.jdbc.password");

        if (!props.containsKey("jakarta.persistence.jdbc.url")) {
            String nombre = entorno("DB_NAME");
            if (nombre != null) {
                String host = valorO("DB_HOST", "localhost");
                String puerto = valorO("DB_PORT", "5432");
                props.setProperty("jakarta.persistence.jdbc.url",
                        "jdbc:postgresql://" + host + ":" + puerto + "/" + nombre);
            }
        }

        if (!props.containsKey("jakarta.persistence.jdbc.url")) {
            throw new IllegalStateException(
                    "Falta la conexion. Configura db.properties o DB_URL, DB_USER y DB_PASSWORD.");
        }

        Map<String, Object> resultado = new HashMap<>();
        props.forEach((clave, valor) -> resultado.put(clave.toString(), valor));
        return resultado;
    }

    private static void copiarEntorno(Properties props, String variable, String propiedad) {
        String valor = entorno(variable);
        if (valor != null) {
            props.setProperty(propiedad, valor);
        }
    }

    private static String entorno(String clave) {
        String valor = System.getenv(clave);
        return valor == null || valor.isBlank() ? null : valor;
    }

    private static String valorO(String clave, String predeterminado) {
        String valor = entorno(clave);
        return valor == null ? predeterminado : valor;
    }
}
