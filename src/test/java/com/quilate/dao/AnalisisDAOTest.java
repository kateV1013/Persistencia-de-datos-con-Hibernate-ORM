package com.quilate.dao;
import com.quilate.BaseDatosPrueba;
import com.quilate.model.*;
import com.quilate.service.AnalisisService;
import com.quilate.util.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class AnalisisDAOTest {
    private Long clienteId;
    @BeforeEach void preparar() {
        BaseDatosPrueba.iniciar("analisis_"+System.nanoTime());
        clienteId=Transaccion.ejecutar(em -> {
            Cliente c=new Cliente("Ana","López","88880000","ana@example.com");em.persist(c);
            Pieza p=new Pieza("ANA-1","Anillo",60.0,100.0,30,null);em.persist(p);
            for(int i=0;i<5;i++) {
                LocalDateTime fecha=LocalDateTime.of(2026,11,i==2?11:10,i==1?23:9,i==1?59:0,i==1?59:0);
                EstadoVenta estado=i==3?EstadoVenta.ANULADA:i==4?EstadoVenta.PREPARACION:EstadoVenta.CONFIRMADA;
                Venta v=new Venta(c,fecha,"EFECTIVO",estado);
                v.agregarDetalle(new DetalleVenta(p,1,new BigDecimal("100"),new BigDecimal("60"),new BigDecimal("10")));
                if(i==0) v.agregarDetalle(new DetalleVenta(p,2,new BigDecimal("100"),new BigDecimal("60"),new BigDecimal("10")));
                em.persist(v);
            }
            // Cambiar inventario no debe cambiar el costo histórico de los reportes.
            p.setCosto(999.0);em.flush();return c.getId();
        });
    }
    @AfterEach void cerrar(){JPAUtil.cerrar();}
    @Test void filtraEstadosEIncluyeElUltimoSegundoDelDia() {
        var r=new AnalisisService().consultar(LocalDate.of(2026,11,10),LocalDate.of(2026,11,10));
        assertEquals(2,r.total().ventas());assertEquals(4,r.total().unidades());
        assertEquals(0,new BigDecimal("360").compareTo(r.total().ingreso()));
        assertEquals(0,new BigDecimal("240").compareTo(r.total().costo()));
        assertEquals(0,new BigDecimal("120").compareTo(r.total().ganancia()));
    }
    @Test void obtienePerfilYRechazaRangoInvertido() {
        assertEquals(3,new AnalisisService().perfil(clienteId).total().ventas());
        assertThrows(RuntimeException.class,()->new AnalisisService().consultar(LocalDate.of(2026,11,11),LocalDate.of(2026,11,10)));
    }
}
