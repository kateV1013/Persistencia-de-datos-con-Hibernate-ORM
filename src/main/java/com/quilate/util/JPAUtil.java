package com.quilate.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class JPAUtil {

    private static volatile SessionFactory emf;

    private JPAUtil() {
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        SessionFactory actual = emf;
        if (actual == null || !actual.isOpen()) {
            synchronized (JPAUtil.class) {
                actual = emf;
                if (actual == null || !actual.isOpen()) {
                    emf = actual = crearFabrica(cargarPropiedades());
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
        emf = crearFabrica(propiedades);
    }

    private static SessionFactory crearFabrica(Map<String, Object> propiedades) {
        var builder = new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml");
        builder.applySettings(propiedades);
        if (!propiedades.containsKey("jakarta.persistence.jdbc.driver")
                && propiedades.get("jakarta.persistence.jdbc.url") instanceof String url
                && url.startsWith("jdbc:mysql:")) {
            builder.applySetting("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        }
        copiarConexion(builder, propiedades, "driver", "driver_class");
        copiarConexion(builder, propiedades, "url", "url");
        copiarConexion(builder, propiedades, "user", "username");
        copiarConexion(builder, propiedades, "password", "password");
        var registro = builder.build();
        try {
            return new MetadataSources(registro).buildMetadata().buildSessionFactory();
        } catch (RuntimeException | Error e) {
            StandardServiceRegistryBuilder.destroy(registro);
            throw e;
        }
    }

    private static void copiarConexion(StandardServiceRegistryBuilder builder,
                                       Map<String, Object> propiedades, String jpa, String hibernate) {
        Object valor = propiedades.get("jakarta.persistence.jdbc." + jpa);
        if (valor != null) {
            builder.applySetting("hibernate.connection." + hibernate, valor);
        }
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
        copiarEntorno(props, "DB_PASS", "jakarta.persistence.jdbc.password");
        copiarEntorno(props, "DB_PASSWORD", "jakarta.persistence.jdbc.password");

        String modoEsquema = entorno("DB_SCHEMA_MODE");
        if (modoEsquema != null) {
            if (!"validate".equals(modoEsquema) && !"update".equals(modoEsquema)) {
                throw new IllegalArgumentException("DB_SCHEMA_MODE debe ser validate o update.");
            }
            props.setProperty("hibernate.hbm2ddl.auto", modoEsquema);
        }

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
