package com.quilate.model;

/** Estados de una venta. Solo las CONFIRMADAS cuentan en los reportes. */
public enum EstadoVenta {
    PENDIENTE,
    CONFIRMADA,
    ANULADA
}
