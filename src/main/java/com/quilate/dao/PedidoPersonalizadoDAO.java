package com.quilate.dao;

import com.quilate.model.EstadoPedido;
import com.quilate.model.PedidoPersonalizado;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PedidoPersonalizadoDAO extends GenericDAO<PedidoPersonalizado, Integer> {

    /** Carga cliente y tipo de servicio en la misma consulta para mostrarlos en la tabla. */
    private static final String SELECT_CON_DETALLE = """
            select p from PedidoPersonalizado p
            join fetch p.cliente
            join fetch p.tipoServicio
            """;

    public PedidoPersonalizadoDAO() {
        super(PedidoPersonalizado.class);
    }

    /** Pedidos con su cliente y tipo de servicio, del mas reciente al mas antiguo. */
    public List<PedidoPersonalizado> listarConDetalle(boolean incluirCancelados) {
        String hql = SELECT_CON_DETALLE
                + (incluirCancelados ? "" : " where p.estado <> :cancelado")
                + " order by p.fechaProgramada desc, p.id desc";
        return enConsulta(em -> {
            var query = em.createQuery(hql, PedidoPersonalizado.class);
            if (!incluirCancelados) {
                query.setParameter("cancelado", EstadoPedido.CANCELADO);
            }
            return query.getResultList();
        });
    }

    public Optional<PedidoPersonalizado> buscarConDetalle(Integer id) {
        return enConsulta(em -> em
                .createQuery(SELECT_CON_DETALLE + " where p.id = :id", PedidoPersonalizado.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst());
    }

    /**
     * Cuenta los pedidos activos (no cancelados) programados entre dos fechas.
     * Recibe el EntityManager para usarse dentro de la misma transaccion que guarda el pedido.
     *
     * @param excluirId pedido que no se cuenta (el que se esta modificando), o null
     */
    public long contarActivosEntre(EntityManager em, LocalDate desde, LocalDate hasta, Integer excluirId) {
        return em.createQuery("""
                        select count(p) from PedidoPersonalizado p
                        where p.fechaProgramada between :desde and :hasta
                          and p.estado <> :cancelado
                          and p.id <> :excluir
                        """, Long.class)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .setParameter("cancelado", EstadoPedido.CANCELADO)
                .setParameter("excluir", excluirId == null ? -1 : excluirId)
                .getSingleResult();
    }

    public long contarActivosEntre(LocalDate desde, LocalDate hasta) {
        return enConsulta(em -> contarActivosEntre(em, desde, hasta, null));
    }
}
