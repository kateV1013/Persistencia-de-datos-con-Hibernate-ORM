package com.quilate.dao;

import com.quilate.exception.ExistenciasInsuficientesException;
import com.quilate.exception.ValidacionException;
import com.quilate.model.Apartado;
import com.quilate.model.Cliente;
import com.quilate.model.EstadoApartado;
import com.quilate.model.Pieza;
import com.quilate.util.JPAUtil;
import com.quilate.util.Transaccion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.util.List;

public class ApartadoDAO {

    /** Crea el apartado y descuenta la cantidad de existencias (misma transacción). */
    public Apartado crear(Long clienteId, Long piezaId, int cantidad) {
        if (cantidad <= 0) throw new ValidacionException("La cantidad debe ser mayor que cero.");
        return Transaccion.ejecutar(em -> {
            Cliente cliente = em.find(Cliente.class, clienteId);
            if (cliente == null) throw new ValidacionException("El cliente no existe.");
            Pieza pieza = em.find(Pieza.class, piezaId, LockModeType.PESSIMISTIC_WRITE);
            if (pieza == null) throw new ValidacionException("La pieza no existe.");

            int disponibles = pieza.getExistencias();
            if (disponibles < cantidad) {
                throw new ExistenciasInsuficientesException(
                        "Existencias insuficientes. Disponibles: " + disponibles);
            }
            pieza.setExistencias(disponibles - cantidad);

            Apartado apartado = new Apartado(cliente, pieza, cantidad);
            em.persist(apartado);
            return apartado;
        });
    }

    /** Cambia la cantidad ajustando la reserva según la diferencia. */
    public void modificarCantidad(Long apartadoId, int nuevaCantidad) {
        if (nuevaCantidad <= 0) throw new ValidacionException("La cantidad debe ser mayor que cero.");
        Transaccion.ejecutar(em -> {
            Apartado apartado = em.find(Apartado.class, apartadoId, LockModeType.PESSIMISTIC_WRITE);
            if (apartado == null) throw new ValidacionException("El apartado no existe.");
            if (apartado.getEstado() != EstadoApartado.ACTIVO) {
                throw new ValidacionException("Solo se pueden modificar apartados activos.");
            }
            Pieza pieza = em.find(Pieza.class, apartado.getPieza().getId(), LockModeType.PESSIMISTIC_WRITE);

            int diferencia = nuevaCantidad - apartado.getCantidad();
            if (diferencia > 0 && pieza.getExistencias() < diferencia) {
                throw new ExistenciasInsuficientesException(
                        "Existencias insuficientes. Disponibles: " + pieza.getExistencias());
            }
            pieza.setExistencias(pieza.getExistencias() - diferencia); // diferencia negativa devuelve
            apartado.setCantidad(nuevaCantidad);
            return null;
        });
    }

    /** Cancela el apartado y devuelve la cantidad reservada a existencias. */
    public void cancelar(Long apartadoId) {
        Transaccion.ejecutar(em -> {
            Apartado apartado = em.find(Apartado.class, apartadoId, LockModeType.PESSIMISTIC_WRITE);
            if (apartado == null) throw new ValidacionException("El apartado no existe.");
            if (apartado.getEstado() != EstadoApartado.ACTIVO) {
                throw new ValidacionException("Solo se pueden cancelar apartados activos.");
            }
            Pieza pieza = em.find(Pieza.class, apartado.getPieza().getId(), LockModeType.PESSIMISTIC_WRITE);
            pieza.setExistencias(pieza.getExistencias() + apartado.getCantidad());
            apartado.setEstado(EstadoApartado.CANCELADO);
            return null;
        });
    }

    /** JOIN FETCH evita LazyInitializationException al mostrar cliente y pieza en la tabla. */
    public List<Apartado> listar() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT a FROM Apartado a "
                                    + "JOIN FETCH a.cliente JOIN FETCH a.pieza "
                                    + "ORDER BY a.fecha DESC", Apartado.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
