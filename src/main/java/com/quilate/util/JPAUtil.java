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

/**
 * Crea un unico EntityManagerFactory para toda la aplicacion.
 * Las credenciales se leen de "db.properties" (raiz del proyecto, ignorado por Git)
 * o de variables de entorno DB_URL, DB_USER y DB_PASSWORD.
 */
public final class JPAUtil {

    private static final String PERSISTENCE_UNIT = "quilatePU";
    private static EntityManagerFactory emf;

    private JPAUtil() {
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, cargarPropiedades());
        }
        return emf;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static synchronized void cerrar() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }

    private static Map<String, Object> cargarPropiedades() {
        Properties props = new Properties();

        Path archivo = Path.of("db.properties");
        if (Files.exists(archivo)) {
            try (InputStream in = Files.newInputStream(archivo)) {
                props.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo leer db.properties", e);
            }
        }

        sobrescribirConEntorno(props, "DB_URL", "jakarta.persistence.jdbc.url");
        sobrescribirConEntorno(props, "DB_USER", "jakarta.persistence.jdbc.user");
        sobrescribirConEntorno(props, "DB_PASSWORD", "jakarta.persistence.jdbc.password");

        if (!props.containsKey("jakarta.persistence.jdbc.url")) {
            throw new IllegalStateException(
                    "Falta la configuracion de la base de datos. Copia db.properties.example como db.properties.");
        }

        Map<String, Object> mapa = new HashMap<>();
        props.forEach((k, v) -> mapa.put(k.toString(), v));
        return mapa;
    }

    private static void sobrescribirConEntorno(Properties props, String variable, String propiedad) {
        String valor = System.getenv(variable);
        if (valor != null && !valor.isBlank()) {
            props.setProperty(propiedad, valor);
        }
    }
}
