package com.quilate.service;
import com.quilate.dao.PiezaDAO;
import com.quilate.exception.ReglaNegocioException;
import com.quilate.model.Pieza;
/** Un lector USB que emula teclado entrega el código y finaliza con Enter. */
public class EscaneoService {
    public Pieza buscar(String codigo) {
        if(codigo==null || codigo.isBlank()) throw new ReglaNegocioException("Escanea o escribe el código de la pieza.");
        return new PiezaDAO().buscarPorCodigoBarras(codigo.trim())
            .orElseThrow(() -> new ReglaNegocioException("No existe una pieza con ese código de barras."));
    }
    public static int cantidadTrasLectura(int actual,int existencias) {
        if(actual<0 || actual>=existencias) throw new ReglaNegocioException("No hay existencias suficientes para añadir otra unidad. Disponible: "+existencias);
        return actual+1;
    }
}
