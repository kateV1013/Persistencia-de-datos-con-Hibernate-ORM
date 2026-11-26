package com.quilate.service;

import com.quilate.dao.ApartadoDAO;
import com.quilate.exception.ValidacionException;
import com.quilate.model.Apartado;

import java.util.List;

public class ApartadoService {

    private final ApartadoDAO dao = new ApartadoDAO();

    public Apartado apartar(Long clienteId, Long piezaId, int cantidad) {
        if (clienteId == null) throw new ValidacionException("Selecciona un cliente.");
        if (piezaId == null) throw new ValidacionException("Selecciona una pieza.");
        validarCantidad(cantidad);
        return dao.crear(clienteId, piezaId, cantidad);
    }

    public void modificarCantidad(Long apartadoId, int nuevaCantidad) {
        if (apartadoId == null) throw new ValidacionException("Selecciona un apartado.");
        validarCantidad(nuevaCantidad);
        dao.modificarCantidad(apartadoId, nuevaCantidad);
    }

    public void cancelar(Long apartadoId) {
        if (apartadoId == null) throw new ValidacionException("Selecciona un apartado.");
        dao.cancelar(apartadoId);
    }

    public List<Apartado> listar() {
        return dao.listar();
    }

    private void validarCantidad(int cantidad) {
        if (cantidad <= 0) throw new ValidacionException("La cantidad debe ser un entero mayor que cero.");
    }
}