package com.quilate.service;

import com.quilate.dao.ApartadoDAO;
import com.quilate.exception.ValidacionException;
import com.quilate.model.Apartado;
import com.quilate.model.HistorialCompra;

import java.math.BigDecimal;
import java.util.List;

public class ApartadoService {

    private final ApartadoDAO dao = new ApartadoDAO();

    public Apartado apartar(Long clienteId, Long piezaId, int cantidad) {
        if (clienteId == null) throw new ValidacionException("Seleccione un cliente.");
        if (piezaId == null) throw new ValidacionException("Seleccione una pieza.");
        validarCantidad(cantidad);
        return dao.crear(clienteId, piezaId, cantidad);
    }

    public void modificarCantidad(Long apartadoId, int nuevaCantidad) {
        if (apartadoId == null) throw new ValidacionException("Seleccione un apartado.");
        validarCantidad(nuevaCantidad);
        dao.modificarCantidad(apartadoId, nuevaCantidad);
    }

    public void cancelar(Long apartadoId) {
        if (apartadoId == null) throw new ValidacionException("Seleccione un apartado.");
        dao.cancelar(apartadoId);
    }

    public void completar(Long apartadoId) {
        if (apartadoId == null) throw new ValidacionException("Seleccione un apartado.");
        dao.completar(apartadoId);
    }

    public List<Apartado> listar() {
        return dao.listar();
    }

    public List<HistorialCompra> historial(Long clienteId) {
        if (clienteId == null) throw new ValidacionException("Seleccione un cliente.");
        return dao.historialPorCliente(clienteId);
    }

    public BigDecimal totalHistorial(List<HistorialCompra> historial) {
        return historial.stream().map(HistorialCompra::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validarCantidad(int cantidad) {
        if (cantidad <= 0) throw new ValidacionException("La cantidad debe ser un entero mayor que cero.");
    }
}