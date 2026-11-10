package com.quilate.dao;

import com.quilate.exception.EntidadConDependenciasException;
import com.quilate.model.Cliente;
import com.quilate.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class ClienteDAO {

    public Cliente crear(Cliente cliente) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(cliente);
            tx.commit();
            return cliente;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Cliente buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Cliente.class, id);
        } finally {
            em.close();
        }
    }

    public List<Cliente> listar() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Cliente c ORDER BY c.nombre", Cliente.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Cliente modificar(Cliente cliente) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Cliente actualizado = em.merge(cliente);
            tx.commit();
            return actualizado;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** No permite eliminar un cliente que ya tiene apartados. */
    public void eliminar(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Long total = em.createQuery(
                            "SELECT COUNT(a) FROM Apartado a WHERE a.cliente.id = :id", Long.class)
                    .setParameter("id", id)
                    .getSingleResult();
            total += em.createQuery("select count(v) from Venta v where v.cliente.id = :id", Long.class)
                    .setParameter("id", id).getSingleResult();
            total += em.createQuery("select count(p) from PedidoPersonalizado p where p.cliente.id = :id", Long.class)
                    .setParameter("id", id).getSingleResult();
            if (total > 0) {
                throw new EntidadConDependenciasException(
                        "No se puede eliminar: el cliente tiene " + total + " movimiento(s) asociado(s).");
            }
            Cliente cliente = em.find(Cliente.class, id);
            if (cliente != null) em.remove(cliente);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
