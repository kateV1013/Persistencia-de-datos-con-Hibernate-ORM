package com.quilate;

import com.quilate.util.JPAUtil;

import java.util.Map;

/** Configura Hibernate contra una base H2 en memoria, nueva para cada clase de prueba. */
public final class BaseDatosPrueba {

    private BaseDatosPrueba() {
    }

    public static void iniciar(String nombre) {
        JPAUtil.inicializar(Map.of(
                "jakarta.persistence.jdbc.driver", "org.h2.Driver",
                "jakarta.persistence.jdbc.url", "jdbc:h2:mem:" + nombre + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "jakarta.persistence.jdbc.user", "sa",
                "jakarta.persistence.jdbc.password", "",
                "hibernate.hbm2ddl.auto", "create-drop",
                "hibernate.show_sql", "false"));
    }
}
