package com.quilate.model;

public enum EstadoApartado {
    ACTIVO,      // cantidad reservada (descontada de existencias)
    CANCELADO,   // reserva devuelta al inventario
    COMPLETADO   // cuenta como compra en el historial
}