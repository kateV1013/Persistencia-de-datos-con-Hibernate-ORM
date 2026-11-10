package com.quilate.service;

import com.quilate.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculadoraVentaTest {

    @Test
    void calculaDescuentoMaximoSinVenderDebajoDelCosto() {
        BigDecimal maximo = CalculadoraVenta.calcularDescuentoMaximo(
                new BigDecimal("100.00"),
                new BigDecimal("70.00")
        );

        assertEquals(new BigDecimal("30.00"), maximo);
    }

    @Test
    void permiteAplicarExactamenteElDescuentoMaximo() {
        BigDecimal subtotal = CalculadoraVenta.calcularSubtotal(
                new BigDecimal("100.00"),
                new BigDecimal("70.00"),
                new BigDecimal("30.00"),
                2
        );

        assertEquals(new BigDecimal("140.00"), subtotal);
    }

    @Test
    void rechazaDescuentoQueDejaElPrecioDebajoDelCosto() {
        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> CalculadoraVenta.calcularSubtotal(
                        new BigDecimal("100.00"),
                        new BigDecimal("70.00"),
                        new BigDecimal("30.01"),
                        1
                )
        );

        assertTrue(error.getMessage().contains("30.00"));
    }

    @Test
    void rechazaCantidadesNoPositivas() {
        assertThrows(
                ReglaNegocioException.class,
                () -> CalculadoraVenta.validarCantidad(0)
        );
        assertThrows(
                ReglaNegocioException.class,
                () -> CalculadoraVenta.validarCantidad(-1)
        );
    }

    @Test
    void sumaYRedondeaLosSubtotales() {
        BigDecimal total = CalculadoraVenta.calcularTotal(List.of(
                new BigDecimal("10.00"),
                new BigDecimal("20.555")
        ));

        assertEquals(new BigDecimal("30.56"), total);
    }
}
