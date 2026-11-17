package com.quilate.dao;
import com.quilate.dto.LineaAnalisis;
import com.quilate.exception.ReglaNegocioException;
import com.quilate.model.EstadoVenta;
import com.quilate.util.JPAUtil;
import java.time.LocalDate;
import java.util.List;

public class AnalisisDAO {
    /** Consulta proyección HQL: nunca usa el precio o costo actual de inventario. */
    public List<LineaAnalisis> consultar(LocalDate desde, LocalDate hasta, Long clienteId) {
        if (desde != null && hasta != null && desde.isAfter(hasta))
            throw new ReglaNegocioException("La fecha inicial debe ser anterior o igual a la final.");
        String hql = """
            select new com.quilate.dto.LineaAnalisis(v.id, c.id, trim(concat(c.nombre, ' ', coalesce(c.apellido, ''))),
                v.fechaHora, p.id, p.nombre, d.cantidad, d.precioUnitario, d.costoUnitarioVenta, d.descuentoAplicado)
            from DetalleVenta d join d.venta v join v.cliente c join d.pieza p
            where v.estado = :estado
            """;
        if (desde != null) hql += " and v.fechaHora >= :desde";
        if (hasta != null) hql += " and v.fechaHora < :hasta";
        if (clienteId != null) hql += " and c.id = :cliente";
        hql += " order by v.fechaHora desc, v.id desc, d.id";
        try (var em = JPAUtil.getEntityManager()) {
            var q = em.createQuery(hql, LineaAnalisis.class).setParameter("estado", EstadoVenta.CONFIRMADA);
            if (desde != null) q.setParameter("desde", desde.atStartOfDay());
            if (hasta != null) q.setParameter("hasta", hasta.plusDays(1).atStartOfDay());
            if (clienteId != null) q.setParameter("cliente", clienteId);
            return q.getResultList();
        }
    }
}
