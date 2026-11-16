package com.quilate.service;
import com.quilate.model.DisenoPieza;
import com.quilate.exception.ReglaNegocioException;
import java.math.BigDecimal;
import java.util.Set;
public final class DisenoService {
    private DisenoService() { }
    public static void validar(DisenoPieza d) {
        if(d==null)return;
        if(!Set.of("Anillo","Placa").contains(d.getForma()==null?"":d.getForma())) error("Selecciona Anillo o Placa.");
        if(!Set.of("Oro","Plata","Oro rosa").contains(d.getAcabado()==null?"":d.getAcabado())) error("Selecciona un acabado válido.");
        if(!Set.of("Clásica","Moderna","Cursiva").contains(d.getFuente()==null?"":d.getFuente())) error("Selecciona una tipografía válida.");
        if(d.getTexto()==null || d.getTexto().codePointCount(0,d.getTexto().length())>40 || d.getTexto().contains("\n")) error("El grabado admite hasta 40 caracteres en una sola línea.");
        boolean anillo="Anillo".equals(d.getForma());
        rango(d.getMedida(),anillo?12:15,anillo?28:60,"medida");
        rango(d.getAncho(),anillo?2:8,anillo?12:35,"ancho");
        rango(d.getLetra(),1,4,"tamaño de letra");
        if(d.getLetra().compareTo(d.getAncho())>0) error("La letra no puede ser más alta que la banda o placa.");
    }
    private static void rango(BigDecimal valor,int min,int max,String campo) {
        if(valor==null || valor.compareTo(BigDecimal.valueOf(min))<0 || valor.compareTo(BigDecimal.valueOf(max))>0)
            error("La "+campo+" debe estar entre "+min+" y "+max+" mm.");
    }
    private static void error(String mensaje) { throw new ReglaNegocioException(mensaje); }
}
