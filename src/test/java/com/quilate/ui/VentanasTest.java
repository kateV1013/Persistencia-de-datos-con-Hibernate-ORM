package com.quilate.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VentanasTest {

    @Test void recargaSoloAlLlegarDesdeOtroModulo() {
        assertTrue(Ventanas.debeRecargar(Modulo.VENTAS, Modulo.INVENTARIO), "de Ventas a Inventario");
        assertFalse(Ventanas.debeRecargar(Modulo.INVENTARIO, Modulo.INVENTARIO), "al cerrar un diálogo propio");
        assertFalse(Ventanas.debeRecargar(null, Modulo.INVENTARIO), "primera ventana abierta");
    }
}
