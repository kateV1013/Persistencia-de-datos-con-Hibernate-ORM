package com.quilate.service;
import com.quilate.BaseDatosPrueba;
import com.quilate.model.Pieza;
import com.quilate.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
class EscaneoServiceTest {
    @BeforeEach void preparar(){BaseDatosPrueba.iniciar("escaner_"+System.nanoTime());Transaccion.ejecutar(em->{em.persist(new Pieza("001234","Anillo",50.0,100.0,2,null));return null;});}
    @AfterEach void cerrar(){JPAUtil.cerrar();}
    @Test void conservaCerosInicialesYBuscaCodigoExacto(){assertEquals("Anillo",new EscaneoService().buscar(" 001234 ").getNombre());assertThrows(RuntimeException.class,()->new EscaneoService().buscar("1234"));}
    @Test void sumaCadaLecturaYBloqueaExceso(){int n=EscaneoService.cantidadTrasLectura(0,2);assertEquals(2,EscaneoService.cantidadTrasLectura(n,2));assertThrows(RuntimeException.class,()->EscaneoService.cantidadTrasLectura(2,2));assertThrows(RuntimeException.class,()->EscaneoService.cantidadTrasLectura(0,0));}
}
