package com.quilate.service;

import com.quilate.exception.ReglaNegocioException;
import com.quilate.model.EstadoVenta;

public final class PoliticaEstadoVenta {

    private PoliticaEstadoVenta() {
    }

    public static void validarModificable(EstadoVenta estadoActual) {
        validarEstado(estadoActual);
        if (estadoActual != EstadoVenta.PREPARACION) {
            throw new ReglaNegocioException(
                    "Solo las ventas en preparación pueden modificarse."
            );
        }
    }

    public static EstadoVenta confirmar(EstadoVenta estadoActual) {
        validarConfirmable(estadoActual);
        return EstadoVenta.CONFIRMADA;
    }

    /** Como {@link #validarModificable}, pero con un mensaje que dice por qué no se confirma. */
    public static void validarConfirmable(EstadoVenta estadoActual) {
        validarEstado(estadoActual);
        if (estadoActual == EstadoVenta.CONFIRMADA) {
            throw new ReglaNegocioException("Esta venta ya fue confirmada.");
        }
        if (estadoActual == EstadoVenta.ANULADA) {
            throw new ReglaNegocioException("Esta venta está anulada; no se puede confirmar.");
        }
        validarModificable(estadoActual);
    }

    public static EstadoVenta anular(EstadoVenta estadoActual) {
        validarEstado(estadoActual);
        if (estadoActual != EstadoVenta.CONFIRMADA) {
            throw new ReglaNegocioException(
                    "Solo una venta confirmada puede anularse."
            );
        }
        return EstadoVenta.ANULADA;
    }


    public static boolean debeReintegrarInventario(EstadoVenta estadoAnterior,
                                                    EstadoVenta estadoNuevo) {
        validarEstado(estadoAnterior);
        validarEstado(estadoNuevo);
        return estadoAnterior == EstadoVenta.CONFIRMADA
                && estadoNuevo == EstadoVenta.ANULADA;
    }

    private static void validarEstado(EstadoVenta estado) {
        if (estado == null) {
            throw new ReglaNegocioException("El estado de la venta es obligatorio.");
        }
    }
}
