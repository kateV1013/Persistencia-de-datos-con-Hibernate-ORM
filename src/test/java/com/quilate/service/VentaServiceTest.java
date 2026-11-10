package com.quilate.service;

import com.quilate.BaseDatosPrueba;
import com.quilate.dao.ApartadoDAO;
import com.quilate.exception.ReglaNegocioException;
import com.quilate.model.Apartado;
import com.quilate.model.Cliente;
import com.quilate.model.EstadoApartado;
import com.quilate.model.EstadoVenta;
import com.quilate.model.Pieza;
import com.quilate.model.Venta;
import com.quilate.util.JPAUtil;
import com.quilate.util.Transaccion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VentaServiceTest {

    private Long clienteId;
    private Long piezaId;
    private VentaService service;

    @BeforeEach
    void preparar() {
        BaseDatosPrueba.iniciar("ventas_" + System.nanoTime());
        Long[] ids = Transaccion.ejecutar(em -> {
            Cliente cliente = new Cliente("Ana", "Lopez", "55550000", "ana@correo.com");
            Pieza pieza = new Pieza("COD-VENTA-1", "Anillo", "Oro",
                    new BigDecimal("60.00"), new BigDecimal("100.00"), "ANILLO", 5);
            em.persist(cliente);
            em.persist(pieza);
            em.flush();
            return new Long[]{cliente.getId(), pieza.getId()};
        });
        clienteId = ids[0];
        piezaId = ids[1];
        service = new VentaService();
    }

    @AfterEach
    void cerrar() {
        JPAUtil.cerrar();
    }

    @Test
    void confirmaVentaYDescuentaStockEnUnaTransaccion() {
        Venta borrador = service.crearEnPreparacion(clienteId, List.of(
                new SolicitudDetalleVenta(piezaId, 2, new BigDecimal("10.00"))));

        Venta confirmada = service.confirmar(borrador.getId());

        assertEquals(EstadoVenta.CONFIRMADA, confirmada.getEstado());
        assertEquals(new BigDecimal("180.00"), confirmada.getTotal());
        assertEquals(3, existencias());
    }

    @Test
    void revierteTodoSiNoHayStock() {
        Venta borrador = service.crearEnPreparacion(clienteId, List.of(
                new SolicitudDetalleVenta(piezaId, 6, BigDecimal.ZERO)));

        assertThrows(ReglaNegocioException.class, () -> service.confirmar(borrador.getId()));

        assertEquals(5, existencias());
        assertEquals(EstadoVenta.PREPARACION,
                service.buscarConDetalles(borrador.getId()).getEstado());
    }

    @Test
    void anulaYDevuelveExistenciasSoloUnaVez() {
        Venta borrador = service.crearEnPreparacion(clienteId, List.of(
                new SolicitudDetalleVenta(piezaId, 2, BigDecimal.ZERO)));
        service.confirmar(borrador.getId());

        service.anular(borrador.getId());
        assertEquals(5, existencias());
        assertThrows(ReglaNegocioException.class, () -> service.anular(borrador.getId()));
        assertEquals(5, existencias());
    }

    @Test
    void convierteApartadoSinDescontarDosVeces() {
        Apartado apartado = new ApartadoDAO().crear(clienteId, piezaId, 2);
        assertEquals(3, existencias());
        Venta borrador = service.crearEnPreparacion(clienteId, List.of(
                new SolicitudDetalleVenta(piezaId, 2, BigDecimal.ZERO)));

        service.confirmarDesdeApartado(borrador.getId(), apartado.getId());

        assertEquals(3, existencias());
        EstadoApartado estado = Transaccion.ejecutar(em ->
                em.find(Apartado.class, apartado.getId()).getEstado());
        assertEquals(EstadoApartado.COMPLETADO, estado);
        service.anular(borrador.getId());
        assertEquals(5, existencias());
    }

    private int existencias() {
        return Transaccion.ejecutar(em -> em.find(Pieza.class, piezaId).getExistencias());
    }
}
