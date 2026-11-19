package com.quilate.dao;

import com.quilate.dto.PiezaMasVendida;
import com.quilate.model.EstadoVenta;
import com.quilate.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

/** Consultas HQL para los reportes. */
public class ReporteDAO {

    /**
     * Consulta HQL entre entidades relacionadas (DetalleVenta -> Venta y DetalleVenta -> Pieza):
     * piezas mas vendidas, sumando las cantidades de los detalles de ventas CONFIRMADAS.
     */
    private static final String HQL_PIEZAS_MAS_VENDIDAS = """
            select new com.quilate.dto.PiezaMasVendida(
                       p.id, p.codigoBarras, p.nombre,
                       sum(d.cantidad),
                       sum(d.cantidad * (d.precioUnitario - d.descuentoAplicado)))
            from DetalleVenta d
            join d.venta v
            join d.pieza p
            where v.estado = :confirmada
            group by p.id, p.codigoBarras, p.nombre
            order by sum(d.cantidad) desc, p.nombre
            """;

    public List<PiezaMasVendida> piezasMasVendidas(int limite) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(HQL_PIEZAS_MAS_VENDIDAS, PiezaMasVendida.class)
                    .setParameter("confirmada", EstadoVenta.CONFIRMADA)
                    .setMaxResults(limite)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
