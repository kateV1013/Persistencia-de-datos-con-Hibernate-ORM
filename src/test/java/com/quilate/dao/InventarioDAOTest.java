package com.quilate.dao;

import com.quilate.BaseDatosPrueba;
import com.quilate.exception.EntidadConDependenciasException;
import com.quilate.model.*;
import com.quilate.util.JPAUtil;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class InventarioDAOTest {
    private final PiezaDAO piezas = new PiezaDAO();
    private final CategoriaDAO categorias = new CategoriaDAO();
    private Categoria categoria;

    @BeforeEach void preparar() {
        BaseDatosPrueba.iniciar("inventario_" + System.nanoTime());
        categoria = categorias.guardar(new Categoria("Anillos", "Oro"));
    }
    @AfterEach void cerrar() { JPAUtil.cerrar(); }

    private Pieza pieza() {
        return new Pieza("INV-1", "Anillo", 60.0, 100.0, 5, categoria);
    }

    @Test void creaBuscaEditaYEliminaPiezaYCategoria() {
        Pieza p = piezas.guardar(pieza());
        assertEquals(p.getId(), piezas.buscarPorCodigoBarras("INV-1").orElseThrow().getId());
        p.setNombre("Anillo editado");
        piezas.actualizar(p);
        assertEquals("Anillo editado", piezas.buscarPorId(p.getId()).orElseThrow().getNombre());
        categoria.setNombre("Anillos de oro");
        categorias.actualizar(categoria);
        assertEquals("Anillos de oro", categorias.buscarPorId(categoria.getId()).orElseThrow().getNombre());
        piezas.eliminar(p.getId());
        categorias.eliminar(categoria.getId());
        assertTrue(piezas.buscarPorId(p.getId()).isEmpty());
        assertTrue(categorias.buscarPorId(categoria.getId()).isEmpty());
    }

    @Test void impideEliminarCategoriaConPiezasYPiezaConApartados() {
        Pieza p = piezas.guardar(pieza());
        assertThrows(EntidadConDependenciasException.class, () -> categorias.eliminar(categoria.getId()));
        Cliente c = new ClienteDAO().crear(new Cliente("Ana", "55550000", null));
        new ApartadoDAO().crear(c.getId(), p.getId(), 1);
        assertThrows(EntidadConDependenciasException.class, () -> piezas.eliminar(p.getId()));
        assertTrue(piezas.buscarPorId(p.getId()).isPresent());
    }

    @Test void rechazaImportesNoFinitosAntesDePersistir() {
        Pieza p = pieza();
        p.setCosto(Double.NaN);
        assertThrows(IllegalArgumentException.class, () -> piezas.guardar(p));
        p.setCosto(60.0);
        p.setPrecioVenta(Double.POSITIVE_INFINITY);
        assertThrows(IllegalArgumentException.class, () -> piezas.guardar(p));
        assertTrue(piezas.listarTodas().isEmpty());
    }
}
