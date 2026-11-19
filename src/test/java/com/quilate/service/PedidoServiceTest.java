package com.quilate.service;

import com.quilate.BaseDatosPrueba;
import com.quilate.dao.GenericDAO;
import com.quilate.exception.ReglaNegocioException;
import com.quilate.model.Cliente;
import com.quilate.model.EstadoPedido;
import com.quilate.model.PedidoPersonalizado;
import com.quilate.model.TipoServicio;
import com.quilate.util.JPAUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PedidoServiceTest {

    private final PedidoService service = new PedidoService();
    private final TipoServicioService tipoService = new TipoServicioService();

    private Cliente cliente;
    private TipoServicio grabado;
    /** Un lunes futuro para que la validacion de "fecha pasada" no interfiera. */
    private LocalDate lunes;

    @BeforeEach
    void preparar() {
        BaseDatosPrueba.iniciar("pedidos");
        cliente = new GenericDAO<Cliente, Integer>(Cliente.class) { }
                .guardar(new Cliente("Maria", "Lopez", "88881111", "maria@correo.com"));
        grabado = tipoService.guardar(new TipoServicio("Grabado", "Iniciales", new BigDecimal("300.00")));
        lunes = LocalDate.now().plusWeeks(2).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    @AfterEach
    void cerrar() {
        JPAUtil.cerrar();
    }

    private PedidoPersonalizado nuevo(LocalDate fecha) {
        return new PedidoPersonalizado(cliente, grabado, "Grabar iniciales", fecha, new BigDecimal("300.00"));
    }

    private void llenarSemana(LocalDate desdeLunes) {
        for (int i = 0; i < PedidoService.CUPO_SEMANAL; i++) {
            service.registrar(nuevo(desdeLunes.plusDays(i % 7)));
        }
    }

    @Test
    void permiteHasta20PedidosPorSemanaYBloqueaEl21() {
        llenarSemana(lunes);
        assertEquals(20, service.ocupadosEnSemana(lunes.plusDays(3)));

        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> service.registrar(nuevo(lunes.plusDays(6))));   // domingo, misma semana
        assertTrue(e.getMessage().contains("Ya no hay espacio"), e.getMessage());
    }

    @Test
    void laSemanaSiguienteTieneSuPropioCupo() {
        llenarSemana(lunes);
        PedidoPersonalizado p = service.registrar(nuevo(lunes.plusDays(7)));  // lunes siguiente
        assertTrue(p.getId() != null);
    }

    @Test
    void cancelarLiberaElCupo() {
        llenarSemana(lunes);
        Integer id = service.listar(false).get(0).getId();
        service.cancelar(id);

        assertEquals(19, service.ocupadosEnSemana(lunes));
        service.registrar(nuevo(lunes));
        assertEquals(20, service.ocupadosEnSemana(lunes));
    }

    @Test
    void cambiarFechaASemanaLlenaSeBloquea() {
        llenarSemana(lunes);
        PedidoPersonalizado otro = service.registrar(nuevo(lunes.plusWeeks(1)));

        otro.setFechaProgramada(lunes.plusDays(2));
        otro.setEstado(EstadoPedido.PENDIENTE);
        assertThrows(ReglaNegocioException.class, () -> service.modificar(otro));
    }

    @Test
    void modificarDentroDeLaMismaSemanaLlenaSiSePermite() {
        llenarSemana(lunes);
        PedidoPersonalizado p = service.listar(false).get(0);

        p.setFechaProgramada(lunes.plusDays(5));
        p.setDescripcionTrabajo("Grabar fecha de boda");
        p.setEstado(EstadoPedido.EN_PROCESO);
        PedidoPersonalizado actualizado = service.modificar(p);

        assertEquals(EstadoPedido.EN_PROCESO, actualizado.getEstado());
        assertEquals(20, service.ocupadosEnSemana(lunes));
    }

    @Test
    void validaCamposYFechaPasada() {
        assertThrows(ReglaNegocioException.class,
                () -> service.registrar(nuevo(LocalDate.now().minusDays(1))));

        PedidoPersonalizado sinDescripcion = nuevo(lunes);
        sinDescripcion.setDescripcionTrabajo("  ");
        assertThrows(ReglaNegocioException.class, () -> service.registrar(sinDescripcion));

        PedidoPersonalizado precioNegativo = nuevo(lunes);
        precioNegativo.setPrecio(new BigDecimal("-1"));
        assertThrows(ReglaNegocioException.class, () -> service.registrar(precioNegativo));
    }

    @Test
    void noSeEliminaUnServicioUsadoEnPedidos() {
        service.registrar(nuevo(lunes));
        assertThrows(ReglaNegocioException.class, () -> tipoService.eliminar(grabado.getId()));
    }

    @Test
    void noPermiteNombresDeServicioRepetidos() {
        assertThrows(ReglaNegocioException.class,
                () -> tipoService.guardar(new TipoServicio("grabado", null, BigDecimal.TEN)));
    }

    @Test
    void unPedidoNoRegresaAUnEstadoAnterior() {
        PedidoPersonalizado p = service.registrar(nuevo(lunes));
        p.setEstado(EstadoPedido.TERMINADO);
        service.modificar(p);

        p.setEstado(EstadoPedido.PENDIENTE);
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> service.modificar(p));
        assertTrue(e.getMessage().contains("no puede regresar"), e.getMessage());
    }

    @Test
    void unPedidoEntregadoYaNoSeModifica() {
        PedidoPersonalizado p = service.registrar(nuevo(LocalDate.now()));
        p.setEstado(EstadoPedido.ENTREGADO);
        service.modificar(p);

        p.setDescripcionTrabajo("Cambiar el grabado");
        assertThrows(ReglaNegocioException.class, () -> service.modificar(p));
    }

    @Test
    void elServicioRechazaPrecioBaseNegativo() {
        assertThrows(ReglaNegocioException.class,
                () -> tipoService.guardar(new TipoServicio("Limpieza", null, new BigDecimal("-5"))));
    }

    @Test
    void noSeEntregaUnPedidoAntesDeSuFecha() {
        PedidoPersonalizado p = service.registrar(nuevo(lunes));
        p.setEstado(EstadoPedido.ENTREGADO);
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> service.modificar(p));
        assertTrue(e.getMessage().contains("aún no llega"), e.getMessage());
    }

    @Test
    void laFechaNoPuedePasarDeDosMeses() {
        assertThrows(ReglaNegocioException.class,
                () -> service.registrar(nuevo(LocalDate.of(9999, 12, 31))));
        assertThrows(ReglaNegocioException.class,
                () -> service.registrar(nuevo(LocalDate.now().plusMonths(2).plusDays(1))));
        assertTrue(service.registrar(nuevo(LocalDate.now().plusMonths(2))).getId() != null);
        PedidoPersonalizado p = service.registrar(nuevo(lunes));
        p.setFechaProgramada(LocalDate.now().plusMonths(3));
        assertThrows(ReglaNegocioException.class, () -> service.modificar(p));
    }

    @Test
    void elPrecioDebeSerPositivoYDentroDelLimite() {
        PedidoPersonalizado gratis = nuevo(lunes);
        gratis.setPrecio(BigDecimal.ZERO);
        assertThrows(ReglaNegocioException.class, () -> service.registrar(gratis));
        PedidoPersonalizado carisimo = nuevo(lunes);
        carisimo.setPrecio(new BigDecimal("100000000"));
        assertThrows(ReglaNegocioException.class, () -> service.registrar(carisimo));
        assertThrows(ReglaNegocioException.class,
                () -> tipoService.guardar(new TipoServicio("Garantia", null, BigDecimal.ZERO)));
        assertThrows(ReglaNegocioException.class,
                () -> tipoService.guardar(new TipoServicio("Oro macizo", null, new BigDecimal("100000000"))));
    }
}
