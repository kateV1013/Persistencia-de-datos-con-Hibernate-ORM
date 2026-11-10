package com.quilate.dao;

import com.quilate.model.Venta;

import java.util.List;
import java.util.Optional;

public class VentaDAO extends GenericDAO<Venta, Long> {

    public VentaDAO() {
        super(Venta.class);
    }

    public Optional<Venta> buscarConDetalles(Long id) {
        return enConsulta(em -> em.createQuery("""
                        select distinct v
                        from Venta v
                        join fetch v.cliente
                        left join fetch v.detalles d
                        left join fetch d.pieza
                        where v.id = :id
                        """, Venta.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst());
    }

    public List<Venta> listarConCliente() {
        return enConsulta(em -> em.createQuery("""
                        select v
                        from Venta v
                        join fetch v.cliente
                        order by v.fechaHora desc, v.id desc
                        """, Venta.class)
                .getResultList());
    }
}
