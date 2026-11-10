package com.quilate.dao;

import com.quilate.model.TipoServicio;

import java.util.List;

public class TipoServicioDAO extends GenericDAO<TipoServicio, Integer> {

    public TipoServicioDAO() {
        super(TipoServicio.class);
    }

    public List<TipoServicio> listarOrdenados() {
        return enConsulta(em -> em
                .createQuery("from TipoServicio t order by t.nombre", TipoServicio.class)
                .getResultList());
    }

    /** Indica si ya existe otro tipo de servicio con ese nombre (sin distinguir mayusculas). */
    public boolean existeNombre(String nombre, Integer excluirId) {
        Long total = enConsulta(em -> em
                .createQuery("""
                        select count(t) from TipoServicio t
                        where lower(t.nombre) = lower(:nombre) and t.id <> :excluir
                        """, Long.class)
                .setParameter("nombre", nombre.trim())
                .setParameter("excluir", excluirId == null ? -1 : excluirId)
                .getSingleResult());
        return total > 0;
    }

    /** Cantidad de pedidos que usan este tipo de servicio. */
    public long contarPedidos(Integer idTipoServicio) {
        return enConsulta(em -> em
                .createQuery("select count(p) from PedidoPersonalizado p where p.tipoServicio.id = :id", Long.class)
                .setParameter("id", idTipoServicio)
                .getSingleResult());
    }
}
