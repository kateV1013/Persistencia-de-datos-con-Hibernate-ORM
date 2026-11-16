package com.quilate.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/** Valores históricos de una línea de venta confirmada. */
public record LineaAnalisis(Long ventaId, Long clienteId, String cliente, LocalDateTime fecha,
                           Long piezaId, String pieza, int cantidad, BigDecimal precio,
                           BigDecimal costo, BigDecimal descuento) {
    public BigDecimal ingreso() { return precio.subtract(descuento).multiply(BigDecimal.valueOf(cantidad)); }
    public BigDecimal costoTotal() { return costo.multiply(BigDecimal.valueOf(cantidad)); }
    public BigDecimal descuentoTotal() { return descuento.multiply(BigDecimal.valueOf(cantidad)); }
    public BigDecimal ganancia() { return ingreso().subtract(costoTotal()); }
}
