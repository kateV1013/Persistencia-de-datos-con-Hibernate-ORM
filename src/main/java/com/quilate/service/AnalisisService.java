package com.quilate.service;

import com.quilate.dao.AnalisisDAO;
import com.quilate.dto.LineaAnalisis;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

/** Agregación de ventas confirmadas; una venta cuenta una vez aunque incluya varias piezas. */
public class AnalisisService {
    public static final List<String> DIAS = List.of("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo");
    public record Grupo(String nombre, long ventas, long unidades, BigDecimal ingreso,
                        BigDecimal costo, BigDecimal descuento, BigDecimal ganancia) { }
    public record Resumen(List<LineaAnalisis> lineas, Grupo total, List<Grupo> piezas,
                          List<Grupo> dias, List<Grupo> horas, List<Grupo> clientes,
                          List<Grupo> fechas, String diaHabitual, String horaHabitual) { }
    public Resumen consultar(LocalDate desde, LocalDate hasta) {
        return resumir(new AnalisisDAO().consultar(desde, hasta, null));
    }
    public Resumen perfil(Long clienteId) {
        if (clienteId == null) throw new IllegalArgumentException("Se requiere un cliente.");
        return resumir(new AnalisisDAO().consultar(null, null, clienteId));
    }
    public static BigDecimal margen(BigDecimal ganancia, BigDecimal ingreso) {
        return ingreso.signum() == 0 ? BigDecimal.ZERO : ganancia.multiply(BigDecimal.valueOf(100)).divide(ingreso, 2, RoundingMode.HALF_UP);
    }
    public static Resumen resumir(List<LineaAnalisis> entrada) {
        List<LineaAnalisis> filas = List.copyOf(entrada);
        List<Grupo> piezas = agrupar(filas, LineaAnalisis::piezaId, LineaAnalisis::pieza);
        piezas.sort(Comparator.comparingLong(Grupo::unidades).reversed().thenComparing(Grupo::nombre));
        List<Grupo> clientes = agrupar(filas, LineaAnalisis::clienteId, LineaAnalisis::cliente);
        clientes.sort(Comparator.comparing(Grupo::ingreso).reversed().thenComparing(Comparator.comparingLong(Grupo::ventas).reversed()).thenComparing(Grupo::nombre));
        List<Grupo> dias = new ArrayList<>(), horas = new ArrayList<>();
        for (int d=1; d<=7; d++) {
            int dia=d; dias.add(total(DIAS.get(d-1), filas.stream().filter(l -> l.fecha().getDayOfWeek().getValue()==dia).toList()));
        }
        for (int h=0; h<24; h++) {
            int hora=h; horas.add(total(String.format("%02d:00–%02d:59", h,h), filas.stream().filter(l -> l.fecha().getHour()==hora).toList()));
        }
        List<Grupo> fechas = agrupar(filas, l -> l.fecha().toLocalDate(), l -> l.fecha().toLocalDate().toString());
        fechas.sort(Comparator.comparing(Grupo::nombre));
        return new Resumen(filas, total("Total", filas), List.copyOf(piezas), List.copyOf(dias), List.copyOf(horas),
                List.copyOf(clientes), List.copyOf(fechas), frecuentes(dias), frecuentes(horas));
    }
    private static String frecuentes(List<Grupo> grupos) {
        long max=grupos.stream().mapToLong(Grupo::ventas).max().orElse(0);
        if(max==0) return "Sin compras confirmadas";
        return grupos.stream().filter(g -> g.ventas()==max).map(Grupo::nombre).collect(java.util.stream.Collectors.joining(" · "));
    }
    private static <K> List<Grupo> agrupar(List<LineaAnalisis> filas, Function<LineaAnalisis,K> clave, Function<LineaAnalisis,String> nombre) {
        Map<K,List<LineaAnalisis>> grupos=new LinkedHashMap<>();
        for(var l:filas) grupos.computeIfAbsent(clave.apply(l),k -> new ArrayList<>()).add(l);
        return grupos.values().stream().map(lista -> total(nombre.apply(lista.getFirst()),lista))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
    private static Grupo total(String nombre, List<LineaAnalisis> filas) {
        BigDecimal ingreso=BigDecimal.ZERO, costo=BigDecimal.ZERO, descuento=BigDecimal.ZERO;
        Set<Long> ventas=new HashSet<>(); long unidades=0;
        for(var l:filas) { ventas.add(l.ventaId()); unidades+=l.cantidad(); ingreso=ingreso.add(l.ingreso());
            costo=costo.add(l.costoTotal()); descuento=descuento.add(l.descuentoTotal()); }
        return new Grupo(nombre,ventas.size(),unidades,ingreso,costo,descuento,ingreso.subtract(costo));
    }
}
