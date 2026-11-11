package com.quilate.dao;

import com.quilate.exception.ExistenciasInsuficientesException;
import com.quilate.exception.ValidacionException;
import com.quilate.model.Apartado;
import com.quilate.model.Cliente;
import com.quilate.model.EstadoApartado;
import com.quilate.model.HistorialCompra;
import com.quilate.model.Pieza;
import com.quilate.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ApartadoDAO {

    /** Crea el apartado y descuenta la cantidad de existencias (misma transacción). */
    public Apartado crear(Long clienteId, Long piezaId, int cantidad) {
        if (cantidad <= 0) throw new ValidacionException("La cantidad debe ser positiva.");
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
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
            tx.commit();
            return apartado;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Cambia la cantidad ajustando la reserva según la diferencia. */
    public void modificarCantidad(Long apartadoId, int nuevaCantidad) {
        if (nuevaCantidad <= 0) throw new ValidacionException("La cantidad debe ser positiva.");
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
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
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Cancela el apartado y devuelve la cantidad reservada a existencias. */
    public void cancelar(Long apartadoId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Apartado apartado = em.find(Apartado.class, apartadoId, LockModeType.PESSIMISTIC_WRITE);
            if (apartado == null) throw new ValidacionException("El apartado no existe.");
            if (apartado.getEstado() != EstadoApartado.ACTIVO) {
                throw new ValidacionException("Solo se pueden cancelar apartados activos.");
            }
            Pieza pieza = em.find(Pieza.class, apartado.getPieza().getId(), LockModeType.PESSIMISTIC_WRITE);
            pieza.setExistencias(pieza.getExistencias() + apartado.getCantidad());
            apartado.setEstado(EstadoApartado.CANCELADO);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Marca el apartado como comprado (no toca existencias: ya estaban reservadas). */
    public void completar(Long apartadoId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Apartado apartado = em.find(Apartado.class, apartadoId, LockModeType.PESSIMISTIC_WRITE);
            if (apartado == null) throw new ValidacionException("El apartado no existe.");
            if (apartado.getEstado() != EstadoApartado.ACTIVO) {
                throw new ValidacionException("Solo se pueden completar apartados activos.");
            }
            apartado.setEstado(EstadoApartado.COMPLETADO);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
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

    /**
     * HQL: compras confirmadas del cliente, con el precio y descuento históricos.
     * El total de cada fila es cantidad * (precioUnitario - descuentoAplicado).
     */
    public List<HistorialCompra> historialPorCliente(Long clienteId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Object[]> filas = em.createQuery(
                            "SELECT v.fechaHora, p.nombre, d.cantidad, (d.precioUnitario - d.descuentoAplicado) "
                                    + "FROM DetalleVenta d JOIN d.venta v JOIN d.pieza p "
                                    + "WHERE v.cliente.id = :clienteId AND v.estado = :confirmada "
                                    + "ORDER BY v.fechaHora DESC", Object[].class)
                    .setParameter("clienteId", clienteId)
                    .setParameter("confirmada", com.quilate.model.EstadoVenta.CONFIRMADA)
                    .getResultList();

            return filas.stream()
                    .map(f -> new HistorialCompra(
                            (LocalDateTime) f[0],
                            (String) f[1],
                            ((Number) f[2]).intValue(),
                            new BigDecimal(f[3].toString())))
                    .toList();
        } finally {
            em.close();
        }
    }
}
