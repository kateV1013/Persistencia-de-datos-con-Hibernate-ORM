package com.quilate.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoldApiClienteTest {

    @Test
    void calculaPrecioPorGramoSegunQuilates() {
        var precio = new GoldApiCliente.PrecioMetal("XAU", new BigDecimal("3544.62"),
                new BigDecimal("120.0000"), Instant.now());

        assertEquals(new BigDecimal("90.0000"), precio.gramoUsd(18));
        assertEquals(new BigDecimal("70.0000"), precio.gramoUsd(14));
        assertEquals(new BigDecimal("50.0000"), precio.gramoUsd(10));
    }
}
