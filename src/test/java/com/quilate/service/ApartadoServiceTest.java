package com.quilate.service;

import com.quilate.BaseDatosPrueba;
import com.quilate.exception.ValidacionException;
import com.quilate.exception.ExistenciasInsuficientesException;
import com.quilate.model.*;
import com.quilate.util.JPAUtil;
import com.quilate.util.Transaccion;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ApartadoServiceTest {
    private final ApartadoService servicio = new ApartadoService();
    private Long clienteId, piezaId;

    @BeforeEach void preparar() {
        BaseDatosPrueba.iniciar("apartados_" + System.nanoTime());
        Long[] ids = Transaccion.ejecutar(em -> {
            Cliente c = new Cliente("Ana", "55550000", "ana@example.com");
            Pieza p = new Pieza("AP-1", "Anillo", "Oro", new BigDecimal("60"),
                    new BigDecimal("100"), "Anillos", 5);
            em.persist(c); em.persist(p); em.flush();
            return new Long[]{c.getId(), p.getId()};
        });
        clienteId = ids[0]; piezaId = ids[1];
    }

    @AfterEach void cerrar() { JPAUtil.cerrar(); }

    private int stock() {
        return Transaccion.ejecutar(em -> em.find(Pieza.class, piezaId).getExistencias());
    }

    @Test void reservaModificaYCancelaSinDuplicarExistencias() {
        Apartado a = servicio.apartar(clienteId, piezaId, 2);
        assertEquals(3, stock());
        servicio.modificarCantidad(a.getId(), 4);
        assertEquals(1, stock());
        servicio.modificarCantidad(a.getId(), 1);
        assertEquals(4, stock());
        servicio.cancelar(a.getId());
        assertEquals(5, stock());
        assertThrows(ValidacionException.class, () -> servicio.cancelar(a.getId()));
        assertEquals(5, stock());
    }

    @Test void reservaSinStockNoCambiaInventario() {
        assertThrows(ExistenciasInsuficientesException.class, () -> servicio.apartar(clienteId, piezaId, 6));
        assertEquals(5, stock());
        assertTrue(servicio.listar().isEmpty());
    }

    @Test void historialMultiplicaCantidadYExcluyeVentasAnuladas() {
        VentaService ventas = new VentaService();
        Venta v = ventas.crearEnPreparacion(clienteId,
                List.of(new SolicitudDetalleVenta(piezaId, 2, new BigDecimal("10"))));
        assertTrue(servicio.historial(clienteId).isEmpty());
        ventas.confirmar(v.getId());
        var historial = servicio.historial(clienteId);
        assertEquals(1, historial.size());
        assertEquals(0, new BigDecimal("180").compareTo(servicio.totalHistorial(historial)));
        ventas.anular(v.getId());
        assertTrue(servicio.historial(clienteId).isEmpty());
    }
}
