package com.quilate.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistorialCompra(LocalDateTime fecha, String pieza, int cantidad, BigDecimal precioUnitario) {
    public BigDecimal total() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}