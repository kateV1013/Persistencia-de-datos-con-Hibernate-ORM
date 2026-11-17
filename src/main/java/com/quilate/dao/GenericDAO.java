package com.quilate.dao;

import com.quilate.util.JPAUtil;
import com.quilate.util.Transaccion;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class GenericDAO<T, ID> {

    protected final Class<T> entityClass;

    protected GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public T guardar(T entidad) {
        return enTransaccion(em -> {
            em.persist(entidad);
            return entidad;
        });
    }

    public T actualizar(T entidad) {
        return enTransaccion(em -> em.merge(entidad));
    }

    public void eliminar(ID id) {
        enTransaccionSinRetorno(em -> {
            T entidad = em.find(entityClass, id);
            if (entidad == null) {
                throw new IllegalArgumentException("No existe el registro con id " + id);
            }
            em.remove(entidad);
        });
    }

    public Optional<T> buscarPorId(ID id) {
        return enConsulta(em -> Optional.ofNullable(em.find(entityClass, id)));
    }

    public List<T> listarTodos() {
        return enConsulta(em -> em
                .createQuery("from " + entityClass.getSimpleName(), entityClass)
                .getResultList());
    }



    protected <R> R enConsulta(Function<EntityManager, R> operacion) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return operacion.apply(em);
        } finally {
            em.close();
        }
    }

    protected <R> R enTransaccion(Function<EntityManager, R> operacion) {
        return Transaccion.ejecutar(operacion);
    }

    protected void enTransaccionSinRetorno(Consumer<EntityManager> operacion) {
        enTransaccion(em -> {
            operacion.accept(em);
            return null;
        });
    }
}
