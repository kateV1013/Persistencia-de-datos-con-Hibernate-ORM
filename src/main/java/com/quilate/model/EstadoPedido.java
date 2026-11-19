package com.quilate.model;

/**
 * Estados de un pedido personalizado.
 * Todos, excepto CANCELADO, se consideran activos y ocupan cupo en su semana.
 */
public enum EstadoPedido {
    PENDIENTE("Pendiente"),
    EN_PROCESO("En proceso"),
    TERMINADO("Terminado"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoPedido(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public boolean esActivo() {
        return this != CANCELADO;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
