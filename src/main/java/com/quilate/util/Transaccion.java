package com.quilate.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.function.Function;

/**
 * Ejecuta varias operaciones de Hibernate dentro de una sola transaccion.
 * Lo usan los servicios cuando una regla de negocio debe verificarse y
 * guardarse de forma atomica (por ejemplo, revisar el cupo y crear el pedido).
 */
public final class Transaccion {

    private Transaccion() {
    }

    public static <R> R ejecutar(Function<EntityManager, R> operacion) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R resultado = operacion.apply(em);
            tx.commit();
            return resultado;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
