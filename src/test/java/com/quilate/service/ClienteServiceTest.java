package com.quilate.service;

import com.quilate.BaseDatosPrueba;
import com.quilate.exception.EntidadConDependenciasException;
import com.quilate.exception.ValidacionException;
import com.quilate.model.Cliente;
import com.quilate.model.Pieza;
import com.quilate.util.JPAUtil;
import com.quilate.util.Transaccion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ClienteServiceTest {

    private final ClienteService servicio = new ClienteService();

    @BeforeEach
    void preparar() {
        BaseDatosPrueba.iniciar("clientes_" + System.nanoTime());
    }

    @AfterEach
    void cerrar() {
        JPAUtil.cerrar();
    }

    @Test
    void guardaYModificaUnCliente() {
        Cliente c = servicio.guardar(new Cliente("  Ana ", "Lopez", "88880000", "ana@example.com"));
        assertEquals("Ana", c.getNombre());

        c.setTelefono("88881111");
        servicio.guardar(c);   // conserva su propio correo sin marcarlo como repetido

        assertEquals("88881111", servicio.listar().get(0).getTelefono());
    }

    @Test
    void rechazaUnCorreoRepetidoConMensajeClaro() {
        servicio.guardar(new Cliente("Ana", "Lopez", "88880000", "ana@example.com"));

        ValidacionException e = assertThrows(ValidacionException.class,
                () -> servicio.guardar(new Cliente("Otra", "Ana", "88882222", "ANA@example.com")));
        assertTrue(e.getMessage().contains("Ya existe un cliente"), e.getMessage());
    }

    @Test
    void validaFormatoDeTelefonoYCorreo() {
        assertThrows(ValidacionException.class,
                () -> servicio.guardar(new Cliente("Ana", "Lopez", "abc", null)));
        assertThrows(ValidacionException.class,
                () -> servicio.guardar(new Cliente("Ana", "Lopez", null, "sin-arroba")));
    }

    @Test
    void noEliminaUnClienteConMovimientos() {
        Cliente c = servicio.guardar(new Cliente("Ana", "Lopez", "88880000", null));
        Long piezaId = Transaccion.ejecutar(em -> {
            Pieza p = new Pieza("CLI-1", "Anillo", "Oro", new BigDecimal("60"), new BigDecimal("100"), null, 5);
            em.persist(p);
            em.flush();
            return p.getId();
        });
        new ApartadoService().apartar(c.getId(), piezaId, 1);

        assertThrows(EntidadConDependenciasException.class, () -> servicio.eliminar(c.getId()));
    }
}
