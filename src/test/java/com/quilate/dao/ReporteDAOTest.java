package com.quilate.dao;

import com.quilate.BaseDatosPrueba;
import com.quilate.dto.PiezaMasVendida;
import com.quilate.model.Cliente;
import com.quilate.model.DetalleVenta;
import com.quilate.model.EstadoVenta;
import com.quilate.model.Pieza;
import com.quilate.model.Venta;
import com.quilate.util.JPAUtil;
import com.quilate.util.Transaccion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReporteDAOTest {

    @BeforeEach
    void preparar() {
        BaseDatosPrueba.iniciar("reportes");
        Transaccion.ejecutar(em -> {
            Cliente c = new Cliente("Jose", "Martinez", "87772222", "jose@correo.com");
            em.persist(c);
            Pieza anillo = new Pieza("7501000000011", "Anillo solitario", "Oro",
                    new BigDecimal("4500"), new BigDecimal("7000"), "ANILLO", 5);
            Pieza cadena = new Pieza("7501000000028", "Cadena cubana", "Plata",
                    new BigDecimal("1200"), new BigDecimal("2000"), "COLLAR", 10);
            Pieza aretes = new Pieza("7501000000035", "Argollas pequenas", "Oro",
                    new BigDecimal("1800"), new BigDecimal("2600"), "ARETES", 8);
            em.persist(anillo);
            em.persist(cadena);
            em.persist(aretes);

            // Confirmadas: cadena 3+2 = 5, anillo 1, aretes 0
            Venta v1 = new Venta(c, LocalDateTime.now(), "TARJETA", EstadoVenta.CONFIRMADA);
            v1.agregarDetalle(new DetalleVenta(cadena, 3, new BigDecimal("2000"), BigDecimal.ZERO));
            v1.agregarDetalle(new DetalleVenta(anillo, 1, new BigDecimal("7000"), BigDecimal.TEN));
            Venta v2 = new Venta(c, LocalDateTime.now(), "EFECTIVO", EstadoVenta.CONFIRMADA);
            v2.agregarDetalle(new DetalleVenta(cadena, 2, new BigDecimal("2000"), BigDecimal.ZERO));

            // No confirmadas: NO deben contar
            Venta anulada = new Venta(c, LocalDateTime.now(), "EFECTIVO", EstadoVenta.ANULADA);
            anulada.agregarDetalle(new DetalleVenta(anillo, 10, new BigDecimal("7000"), BigDecimal.ZERO));
            Venta pendiente = new Venta(c, LocalDateTime.now(), "EFECTIVO", EstadoVenta.PENDIENTE);
            pendiente.agregarDetalle(new DetalleVenta(aretes, 7, new BigDecimal("2600"), BigDecimal.ZERO));

            em.persist(v1);
            em.persist(v2);
            em.persist(anulada);
            em.persist(pendiente);
            return null;
        });
    }

    @AfterEach
    void cerrar() {
        JPAUtil.cerrar();
    }

    @Test
    void sumaSoloVentasConfirmadasYOrdenaDeMayorAMenor() {
        List<PiezaMasVendida> top = new ReporteDAO().piezasMasVendidas(10);

        assertEquals(2, top.size());
        assertEquals("Cadena cubana", top.get(0).nombre());
        assertEquals(5L, top.get(0).cantidadVendida());
        assertEquals(0, new BigDecimal("10000").compareTo(top.get(0).totalVendido()));   // 5 x 2000
        assertEquals("Anillo solitario", top.get(1).nombre());
        assertEquals(1L, top.get(1).cantidadVendida());
        assertEquals(0, new BigDecimal("6300").compareTo(top.get(1).totalVendido()));    // 7000 - 10 %
    }

    @Test
    void respetaElLimite() {
        assertEquals(1, new ReporteDAO().piezasMasVendidas(1).size());
    }
}
