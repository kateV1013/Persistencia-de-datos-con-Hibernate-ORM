package com.quilate.dto;

import java.math.BigDecimal;

/**
 * Fila del reporte de piezas mas vendidas (resultado de la consulta HQL en ReporteDAO).
 *
 * @param cantidadVendida suma de las cantidades en los detalles de ventas confirmadas
 * @param totalVendido    suma de cantidad x precio unitario, ya con el descuento aplicado
 */
public record PiezaMasVendida(
        Long idPieza,
        String codigoBarras,
        String nombre,
        Long cantidadVendida,
        BigDecimal totalVendido) {
}
