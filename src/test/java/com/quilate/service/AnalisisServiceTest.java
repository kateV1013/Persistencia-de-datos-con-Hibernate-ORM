package com.quilate.service;
import com.quilate.dto.LineaAnalisis;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class AnalisisServiceTest {
    private LineaAnalisis linea(long venta,long cliente,int dia,int hora,String precio,String costo,String descuento) {
        return new LineaAnalisis(venta,cliente,"Cliente "+cliente,LocalDateTime.of(2026,11,dia,hora,0),venta,"Pieza "+venta,2,new BigDecimal(precio),new BigDecimal(costo),new BigDecimal(descuento));
    }
    @Test void cuentaVentasDistintasYSumaDescuentoPorCantidad() {
        var r=AnalisisService.resumir(List.of(linea(1,1,9,10,"100","60","10"),linea(1,1,9,10,"100","60","10")));
        assertEquals(1,r.total().ventas()); assertEquals(4,r.total().unidades());
        assertEquals(new BigDecimal("360"),r.total().ingreso());assertEquals(new BigDecimal("40"),r.total().descuento());
        assertEquals(new BigDecimal("120"),r.total().ganancia());assertEquals("Lunes",r.diaHabitual());
        assertEquals("10:00–10:59",r.horaHabitual());
    }
    @Test void conservaEmpatesYOrdenaClientesPorMonto() {
        var r=AnalisisService.resumir(List.of(linea(1,1,9,10,"100","60","0"),linea(2,2,10,15,"300","100","0")));
        assertEquals("Lunes · Martes",r.diaHabitual());assertEquals("10:00–10:59 · 15:00–15:59",r.horaHabitual());
        assertEquals("Cliente 2",r.clientes().getFirst().nombre());
    }
    @Test void distingueEquilibrioYReporteVacio() {
        var r=AnalisisService.resumir(List.of(linea(1,1,9,10,"100","60","40")));
        assertEquals(0,r.total().ganancia().signum());
        assertEquals(0,AnalisisService.margen(BigDecimal.ZERO,BigDecimal.ZERO).signum());
        assertEquals("Sin compras confirmadas",AnalisisService.resumir(List.of()).horaHabitual());
    }
}
