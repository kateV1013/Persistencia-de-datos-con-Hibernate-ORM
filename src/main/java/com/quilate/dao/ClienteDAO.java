package com.quilate.dao;

import com.quilate.exception.EntidadConDependenciasException;
import com.quilate.model.Cliente;
import com.quilate.util.JPAUtil;
import com.quilate.util.Transaccion;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ClienteDAO {

    public Cliente crear(Cliente cliente) {
        return Transaccion.ejecutar(em -> {
            em.persist(cliente);
            return cliente;
        });
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

    /** Indica si otro cliente ya usa ese correo (sin distinguir mayúsculas). */
    public boolean existeCorreo(String correo, Long excluirId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("""
                            select count(c) from Cliente c
                            where lower(c.correo) = lower(:correo) and c.id <> :excluir
                            """, Long.class)
                    .setParameter("correo", correo)
                    .setParameter("excluir", excluirId == null ? -1L : excluirId)
                    .getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public Cliente modificar(Cliente cliente) {
        return Transaccion.ejecutar(em -> {
            Cliente actualizado = em.merge(cliente);
            return actualizado;
        });
    }

    /** No permite eliminar un cliente que ya tiene apartados. */
    public void eliminar(Long id) {
        Transaccion.ejecutar(em -> {
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
            return null;
        });
    }
}
