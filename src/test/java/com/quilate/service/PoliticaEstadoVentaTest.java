package com.quilate.service;

import com.quilate.exception.ReglaNegocioException;
import com.quilate.model.EstadoVenta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoliticaEstadoVentaTest {

    @Test
    void confirmaUnaVentaEnPreparacion() {
        assertEquals(
                EstadoVenta.CONFIRMADA,
                PoliticaEstadoVenta.confirmar(EstadoVenta.PREPARACION)
        );
    }

    @Test
    void impideModificarUnaVentaConfirmada() {
        assertThrows(
                ReglaNegocioException.class,
                () -> PoliticaEstadoVenta.validarModificable(EstadoVenta.CONFIRMADA)
        );
    }

    @Test
    void soloAnulaVentasConfirmadas() {
        assertEquals(
                EstadoVenta.ANULADA,
                PoliticaEstadoVenta.anular(EstadoVenta.CONFIRMADA)
        );
        assertThrows(
                ReglaNegocioException.class,
                () -> PoliticaEstadoVenta.anular(EstadoVenta.ANULADA)
        );
    }

    @Test
    void reintegraInventarioSoloEnLaPrimeraTransicionAAnulada() {
        assertTrue(PoliticaEstadoVenta.debeReintegrarInventario(
                EstadoVenta.CONFIRMADA,
                EstadoVenta.ANULADA
        ));
        assertFalse(PoliticaEstadoVenta.debeReintegrarInventario(
                EstadoVenta.ANULADA,
                EstadoVenta.ANULADA
        ));
    }
}
