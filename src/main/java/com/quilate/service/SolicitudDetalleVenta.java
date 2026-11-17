package com.quilate.service;

import java.math.BigDecimal;

public record SolicitudDetalleVenta(Long piezaId, int cantidad, BigDecimal descuentoUnitario) {
}
