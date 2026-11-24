package com.quilate.ui;

import com.quilate.model.EstadoApartado;
import com.quilate.model.EstadoPedido;
import com.quilate.model.EstadoVenta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PresentacionTablasTest {

    @Test void insigniaDeEstadoSeLeeEnEspanolConTilde() {
        assertEquals("En preparación", PresentacionTablas.textoEstado(EstadoVenta.PREPARACION));
        assertEquals("Confirmada", PresentacionTablas.textoEstado(EstadoVenta.CONFIRMADA));
        assertEquals("Anulada", PresentacionTablas.textoEstado(EstadoVenta.ANULADA));
        assertEquals("En proceso", PresentacionTablas.textoEstado(EstadoPedido.EN_PROCESO));
        assertEquals("Activo", PresentacionTablas.textoEstado(EstadoApartado.ACTIVO));
    }
}
