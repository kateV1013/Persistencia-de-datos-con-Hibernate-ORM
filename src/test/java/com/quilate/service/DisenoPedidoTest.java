package com.quilate.service;
import com.quilate.BaseDatosPrueba;
import com.quilate.model.*;
import com.quilate.util.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class DisenoPedidoTest {
    private Cliente cliente; private TipoServicio tipo;
    @BeforeEach void preparar(){
        BaseDatosPrueba.iniciar("diseno_pedido_"+System.nanoTime());
        Transaccion.ejecutar(em->{cliente=new Cliente("Luis","Pérez","88881111","luis@example.com");tipo=new TipoServicio("Grabado","Iniciales",new BigDecimal("300"));em.persist(cliente);em.persist(tipo);return null;});
    }
    @AfterEach void cerrar(){JPAUtil.cerrar();}
    @Test void guardaRecuperaModificaYRetiraElDiseno() {
        PedidoService servicio=new PedidoService();
        PedidoPersonalizado pedido=new PedidoPersonalizado(cliente,tipo,"Grabar iniciales",LocalDate.now().plusDays(4),new BigDecimal("300"));
        DisenoPieza diseno=new DisenoPieza();diseno.setTexto("M & J");diseno.setAcabado("Oro rosa");pedido.setDiseno(diseno);
        servicio.registrar(pedido);
        var leido=servicio.listar(false).getFirst();assertEquals("M & J",leido.getDiseno().getTexto());assertEquals("Oro rosa",leido.getDiseno().getAcabado());
        leido.getDiseno().setMedida(new BigDecimal("19.20"));leido.getDiseno().setTexto("Siempre");servicio.modificar(leido);
        leido=servicio.listar(false).getFirst();assertEquals("Siempre",leido.getDiseno().getTexto());assertEquals(new BigDecimal("19.20"),leido.getDiseno().getMedida());
        leido.setDiseno(null);servicio.modificar(leido);assertNull(servicio.listar(false).getFirst().getDiseno());
    }
    @Test void rechazaMedidasInvalidasYSigueAceptandoPedidosSinVistaPrevia() {
        DisenoPieza d=new DisenoPieza();d.setMedida(new BigDecimal("35"));assertThrows(RuntimeException.class,()->DisenoService.validar(d));
        d.setForma("Placa");d.setAncho(new BigDecimal("10"));assertDoesNotThrow(()->DisenoService.validar(d));
        d.setTexto("X".repeat(41));assertThrows(RuntimeException.class,()->DisenoService.validar(d));
        PedidoPersonalizado p=new PedidoPersonalizado(cliente,tipo,"Pulido",LocalDate.now().plusDays(4),new BigDecimal("300"));
        new PedidoService().registrar(p);assertNull(new PedidoService().listar(false).getFirst().getDiseno());
    }
}
