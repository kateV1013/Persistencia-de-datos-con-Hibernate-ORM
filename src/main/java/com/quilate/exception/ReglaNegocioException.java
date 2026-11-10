package com.quilate.exception;

/**
 * Se lanza cuando se viola una regla del negocio: stock insuficiente,
 * cupo semanal de pedidos lleno o descuento que genera perdida.
 * El controlador la atrapa y la muestra con AlertUtil.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
