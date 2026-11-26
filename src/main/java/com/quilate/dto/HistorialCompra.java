package com.quilate.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistorialCompra(LocalDateTime fecha, String pieza, int cantidad, BigDecimal precioUnitarioNeto) {
    public BigDecimal total() {
        return precioUnitarioNeto.multiply(BigDecimal.valueOf(cantidad));
    }
}