package com.quilate.service;

import com.quilate.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;


public final class CalculadoraVenta {

    public static final int DECIMALES_MONEDA = 2;
    private static final RoundingMode REDONDEO = RoundingMode.HALF_UP;

    private CalculadoraVenta() {
    }

    public static BigDecimal calcularDescuentoMaximo(BigDecimal precioUnitario,
                                                      BigDecimal costoUnitario) {
        BigDecimal precio = validarPrecio(precioUnitario);
        BigDecimal costo = validarCosto(costoUnitario);

        if (precio.compareTo(costo) < 0) {
            throw new ReglaNegocioException(
                    "El precio de venta no puede ser menor que el costo unitario."
            );
        }

        return dinero(precio.subtract(costo));
    }

    public static BigDecimal calcularPrecioFinal(BigDecimal precioUnitario,
                                                  BigDecimal costoUnitario,
                                                  BigDecimal descuentoUnitario) {
        BigDecimal precio = validarPrecio(precioUnitario);
        BigDecimal descuento = validarDescuento(
                precio,
                costoUnitario,
                descuentoUnitario
        );
        return dinero(precio.subtract(descuento));
    }

    public static BigDecimal calcularSubtotal(BigDecimal precioUnitario,
                                               BigDecimal costoUnitario,
                                               BigDecimal descuentoUnitario,
                                               int cantidad) {
        validarCantidad(cantidad);
        BigDecimal precioFinal = calcularPrecioFinal(
                precioUnitario,
                costoUnitario,
                descuentoUnitario
        );
        return dinero(precioFinal.multiply(BigDecimal.valueOf(cantidad)));
    }

    public static BigDecimal calcularTotal(Collection<BigDecimal> subtotales) {
        if (subtotales == null) {
            throw new ReglaNegocioException("La lista de subtotales es obligatoria.");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal subtotal : subtotales) {
            if (subtotal == null || subtotal.signum() < 0) {
                throw new ReglaNegocioException(
                        "Los subtotales deben ser valores no negativos."
                );
            }
            total = total.add(subtotal);
        }
        return dinero(total);
    }

    public static void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero.");
        }
    }

    private static BigDecimal validarDescuento(BigDecimal precioUnitario,
                                                BigDecimal costoUnitario,
                                                BigDecimal descuentoUnitario) {
        if (descuentoUnitario == null) {
            throw new ReglaNegocioException("El descuento es obligatorio.");
        }
        if (descuentoUnitario.signum() < 0) {
            throw new ReglaNegocioException("El descuento no puede ser negativo.");
        }

        BigDecimal descuento = dinero(descuentoUnitario);
        BigDecimal maximo = calcularDescuentoMaximo(precioUnitario, costoUnitario);
        if (descuento.compareTo(maximo) > 0) {
            throw new ReglaNegocioException(
                    "El descuento maximo permitido es " + maximo.toPlainString() + "."
            );
        }
        return descuento;
    }

    private static BigDecimal validarPrecio(BigDecimal precio) {
        if (precio == null) {
            throw new ReglaNegocioException("El precio unitario es obligatorio.");
        }
        if (precio.signum() <= 0) {
            throw new ReglaNegocioException("El precio unitario debe ser mayor que cero.");
        }
        return dinero(precio);
    }

    private static BigDecimal validarCosto(BigDecimal costo) {
        if (costo == null) {
            throw new ReglaNegocioException("El costo unitario es obligatorio.");
        }
        if (costo.signum() < 0) {
            throw new ReglaNegocioException("El costo unitario no puede ser negativo.");
        }
        return dinero(costo);
    }

    private static BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(DECIMALES_MONEDA, REDONDEO);
    }
}
