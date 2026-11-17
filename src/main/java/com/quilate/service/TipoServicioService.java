package com.quilate.service;

import com.quilate.dao.TipoServicioDAO;
import com.quilate.exception.ReglaNegocioException;
import com.quilate.model.TipoServicio;

import java.math.BigDecimal;
import java.util.List;

/** CRUD de tipos de servicio con sus validaciones. */
public class TipoServicioService {

    private final TipoServicioDAO dao = new TipoServicioDAO();

    public List<TipoServicio> listar() {
        return dao.listarOrdenados();
    }

    /** Crea el tipo de servicio si no tiene id, o lo actualiza si ya existe. */
    public TipoServicio guardar(TipoServicio tipo) {
        validar(tipo);
        tipo.setNombre(tipo.getNombre().trim());
        if (tipo.getDescripcion() != null) {
            tipo.setDescripcion(tipo.getDescripcion().trim());
        }
        return tipo.getId() == null ? dao.guardar(tipo) : dao.actualizar(tipo);
    }

    public void eliminar(Integer id) {
        long pedidos = dao.contarPedidos(id);
        if (pedidos > 0) {
            throw new ReglaNegocioException("No se puede eliminar: este servicio se usa en "
                    + pedidos + " pedido(s).");
        }
        dao.eliminar(id);
    }

    private void validar(TipoServicio tipo) {
        String nombre = tipo.getNombre();
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre del servicio es obligatorio.");
        }
        if (nombre.trim().length() > 60) {
            throw new ReglaNegocioException("El nombre no puede tener mas de 60 caracteres.");
        }
        if (tipo.getDescripcion() != null && tipo.getDescripcion().trim().length() > 255) {
            throw new ReglaNegocioException("La descripcion no puede tener mas de 255 caracteres.");
        }
        BigDecimal precio = tipo.getPrecioBase();
        if (precio == null) {
            throw new ReglaNegocioException("El precio base es obligatorio.");
        }
        if (precio.signum() < 0) {
            throw new ReglaNegocioException("El precio base no puede ser negativo.");
        }
        if (dao.existeNombre(nombre, tipo.getId())) {
            throw new ReglaNegocioException("Ya existe un servicio llamado \"" + nombre.trim() + "\".");
        }
    }
}
