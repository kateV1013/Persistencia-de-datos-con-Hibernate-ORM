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

    /** Formulario abierto con 5 unidades mientras otra ventana vende 2. */
    private Pieza formularioDesfasado(Pieza guardada, int existenciasEscritas) {
        Pieza formulario = new Pieza("INV-1", "Anillo", 60.0, 120.0, existenciasEscritas, categoria);
        formulario.setId(guardada.getId());
        return formulario;
    }

    @Test void editarPiezaNoPisaExistenciasCambiadasPorOtraVentana() {
        Pieza p = piezas.guardar(pieza());
        piezas.actualizarExistencias(p.getId(), 3);

        piezas.actualizarDatos(formularioDesfasado(p, 5), 5);

        Pieza enBd = piezas.buscarPorId(p.getId()).orElseThrow();
        assertEquals(120.0, enBd.getPrecioVenta());
        assertEquals(3, enBd.getExistencias());
    }

    @Test void editarPiezaAplicaSoloElAjusteDeExistenciasDelUsuario() {
        Pieza p = piezas.guardar(pieza());
        piezas.actualizarExistencias(p.getId(), 3);

        piezas.actualizarDatos(formularioDesfasado(p, 9), 5);

        assertEquals(7, piezas.buscarPorId(p.getId()).orElseThrow().getExistencias());
    }

    @Test void editarPiezaRechazaUnAjusteQueDejariaExistenciasNegativas() {
        Pieza p = piezas.guardar(pieza());
        piezas.actualizarExistencias(p.getId(), 3);

        assertThrows(IllegalArgumentException.class,
                () -> piezas.actualizarDatos(formularioDesfasado(p, 1), 5));
        assertEquals(3, piezas.buscarPorId(p.getId()).orElseThrow().getExistencias());
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

    @Test void rechazaPrecioMenorQueElCosto() {
        Pieza p = pieza();
        p.setPrecioVenta(50.0);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> piezas.guardar(p));
        assertTrue(e.getMessage().contains("menor que el costo"), e.getMessage());
    }

    @Test void editarPiezaGuardaMaterialQuilatesYPeso() {
        Pieza p = piezas.guardar(pieza());
        Pieza formulario = formularioDesfasado(p, 5);
        formulario.setMaterial("Oro");
        formulario.setQuilates((short) 18);
        formulario.setPesoGramos(new java.math.BigDecimal("3.50"));

        piezas.actualizarDatos(formulario, 5);

        Pieza enBd = piezas.buscarPorId(p.getId()).orElseThrow();
        assertEquals("Oro", enBd.getMaterial());
        assertEquals((short) 18, enBd.getQuilates());
        assertEquals(0, new java.math.BigDecimal("3.50").compareTo(enBd.getPesoGramos()));
    }

    @Test void rechazaCategoriaConDescripcionDemasiadoLarga() {
        assertThrows(IllegalArgumentException.class,
                () -> categorias.guardar(new Categoria("Collares", "x".repeat(501))));
    }

    @Test void rechazaDatosFueraDeRango() {
        assertThrows(IllegalArgumentException.class,
                () -> piezas.guardar(new Pieza("AÑO-1", "Anillo", 60.0, 100.0, 1, categoria)));
        assertThrows(IllegalArgumentException.class,
                () -> piezas.guardar(new Pieza("C".repeat(51), "Anillo", 60.0, 100.0, 1, categoria)));
        assertThrows(IllegalArgumentException.class,
                () -> piezas.guardar(new Pieza("N-1", "n".repeat(201), 60.0, 100.0, 1, categoria)));
        assertThrows(IllegalArgumentException.class,
                () -> piezas.guardar(new Pieza("P-1", "Anillo", 60.0, 10_000_001.0, 1, categoria)));
        assertThrows(IllegalArgumentException.class,
                () -> piezas.guardar(new Pieza("E-1", "Anillo", 60.0, 100.0, 100_001, categoria)));
        assertTrue(piezas.listarTodas().isEmpty());
        assertNotNull(piezas.guardar(new Pieza("QL-001", "Anillo", 60.0, 10_000_000.0, 100_000, categoria)).getId());
    }

    @Test void elCodigoDeBarrasNoDistingueMayusculas() {
        Pieza p = piezas.guardar(new Pieza("ql-007", "Anillo", 60.0, 100.0, 1, categoria));
        assertEquals("QL-007", p.getCodigoBarras(), "se guarda en mayúsculas");
        assertEquals(p.getId(), piezas.buscarPorCodigoBarras(" ql-007 ").orElseThrow().getId());
        assertTrue(piezas.existeCodigoBarras("Ql-007", null));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> piezas.guardar(new Pieza("QL-007", "Otro anillo", 60.0, 100.0, 1, categoria)));
        assertEquals("Ya existe una pieza con el código de barras: QL-007", e.getMessage());
        Pieza otra = piezas.guardar(new Pieza("QL-008", "Otro anillo", 60.0, 100.0, 1, categoria));
        Pieza formulario = formularioDesfasado(otra, 1);
        formulario.setCodigoBarras("ql-007");
        assertThrows(IllegalArgumentException.class, () -> piezas.actualizarDatos(formulario, 1));
    }

    @Test void noRepiteCategoriasAunqueCambienLasMayusculas() {
        assertThrows(IllegalArgumentException.class, () -> categorias.guardar(new Categoria("ANILLOS", null)));
        assertThrows(IllegalArgumentException.class, () -> categorias.guardar(new Categoria("  anillos  ", null)));
        Categoria otra = categorias.guardar(new Categoria("Cadenas", null));
        otra.setNombre("anillos");
        assertThrows(IllegalArgumentException.class, () -> categorias.actualizar(otra));
    }
}
