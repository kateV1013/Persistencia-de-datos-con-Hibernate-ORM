package com.quilate.service;

import com.quilate.dao.VentaDAO;
import com.quilate.exception.ReglaNegocioException;
import com.quilate.model.Apartado;
import com.quilate.model.Cliente;
import com.quilate.model.DetalleVenta;
import com.quilate.model.EstadoApartado;
import com.quilate.model.EstadoVenta;
import com.quilate.model.Pieza;
import com.quilate.model.Venta;
import com.quilate.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class VentaService {

    private final VentaDAO ventaDAO;

    public VentaService() {
        this(new VentaDAO());
    }

    VentaService(VentaDAO ventaDAO) {
        this.ventaDAO = ventaDAO;
    }

    public Venta crearEnPreparacion(Long clienteId, List<SolicitudDetalleVenta> solicitudes) {
        return crearEnPreparacion(clienteId, "EFECTIVO", solicitudes);
    }

    public Venta crearEnPreparacion(Long clienteId, String metodoPago,
                                    List<SolicitudDetalleVenta> solicitudes) {
        return enTransaccion(em -> {
            Cliente cliente = requerirCliente(em, clienteId);
            Venta venta = new Venta(cliente);
            venta.setFechaHora(LocalDateTime.now());
            venta.setMetodoPago(validarMetodoPago(metodoPago));
            cargarDetalles(em, venta, solicitudes);
            em.persist(venta);
            return venta;
        });
    }

    public Venta modificarEnPreparacion(Long ventaId, Long clienteId,
                                        List<SolicitudDetalleVenta> solicitudes) {
        return modificarEnPreparacion(ventaId, clienteId, "EFECTIVO", solicitudes);
    }

    public Venta modificarEnPreparacion(Long ventaId, Long clienteId, String metodoPago,
                                        List<SolicitudDetalleVenta> solicitudes) {
        return enTransaccion(em -> {
            Venta venta = requerirVenta(em, ventaId, LockModeType.PESSIMISTIC_WRITE);
            PoliticaEstadoVenta.validarModificable(venta.getEstado());
            venta.setCliente(requerirCliente(em, clienteId));
            venta.setMetodoPago(validarMetodoPago(metodoPago));
            venta.limpiarDetalles();
            cargarDetalles(em, venta, solicitudes);
            return venta;
        });
    }

    public Venta confirmar(Long ventaId) {
        return confirmarInterno(ventaId, null, false);
    }

    /**
     * Confirma una venta cuyo inventario ya fue reservado por un apartado.
     * No vuelve a descontar stock. El id del apartado queda registrado y es unico,
     * de modo que no pueda convertirse dos veces en ventas distintas.
     */
    public Venta confirmarDesdeApartado(Long ventaId, Long apartadoId) {
        if (apartadoId == null || apartadoId <= 0) {
            throw new ReglaNegocioException("El apartado de origen es obligatorio.");
        }
        return confirmarInterno(ventaId, apartadoId, true);
    }

    public Venta convertirApartado(Long apartadoId) {
        return enTransaccion(em -> {
            Apartado a = em.find(Apartado.class, apartadoId, LockModeType.PESSIMISTIC_WRITE);
            if (a == null || a.getEstado() != EstadoApartado.ACTIVO)
                throw new ReglaNegocioException("Seleccione un apartado activo.");
            Venta v = new Venta(a.getCliente());
            cargarDetalles(em, v, List.of(new SolicitudDetalleVenta(a.getPieza().getId(), a.getCantidad(), BigDecimal.ZERO)));
            v.setApartadoOrigen(a);
            v.setInventarioComprometido(true);
            v.setEstado(EstadoVenta.CONFIRMADA);
            a.setEstado(EstadoApartado.COMPLETADO);
            em.persist(v);
            return v;
        });
    }

    public Venta anular(Long ventaId) {
        return enTransaccion(em -> {
            Venta venta = requerirVenta(em, ventaId, LockModeType.PESSIMISTIC_WRITE);
            venta.setEstado(PoliticaEstadoVenta.anular(venta.getEstado()));

            if (!venta.isInventarioComprometido()) {
                throw new ReglaNegocioException(
                        "La venta confirmada no tiene inventario asociado para reintegrar.");
            }

            for (DetalleVenta detalle : venta.getDetalles()) {
                Pieza pieza = em.find(
                        Pieza.class, detalle.getPieza().getId(), LockModeType.PESSIMISTIC_WRITE);
                pieza.setExistencias(pieza.getExistencias() + detalle.getCantidad());
            }
            return venta;
        });
    }

    public List<Venta> listar() {
        return ventaDAO.listarConCliente();
    }

    public Venta buscarConDetalles(Long ventaId) {
        return ventaDAO.buscarConDetalles(ventaId)
                .orElseThrow(() -> new ReglaNegocioException("No existe la venta " + ventaId + "."));
    }

    private Venta confirmarInterno(Long ventaId, Long apartadoId, boolean inventarioYaReservado) {
        return enTransaccion(em -> {
            Venta venta = requerirVenta(em, ventaId, LockModeType.PESSIMISTIC_WRITE);
            PoliticaEstadoVenta.validarModificable(venta.getEstado());
            if (venta.getDetalles().isEmpty()) {
                throw new ReglaNegocioException("La venta debe contener al menos una pieza.");
            }

            Apartado apartado = null;
            if (inventarioYaReservado) {
                apartado = em.find(Apartado.class, apartadoId, LockModeType.PESSIMISTIC_WRITE);
                if (apartado == null) {
                    throw new ReglaNegocioException("No existe el apartado " + apartadoId + ".");
                }
                if (apartado.getEstado() != EstadoApartado.ACTIVO) {
                    throw new ReglaNegocioException("Solo un apartado activo puede convertirse en venta.");
                }
                Long usos = em.createQuery("""
                                select count(v)
                                from Venta v
                                where v.apartadoOrigen.id = :apartadoId and v.id <> :ventaId
                                """, Long.class)
                        .setParameter("apartadoId", apartadoId)
                        .setParameter("ventaId", ventaId)
                        .getSingleResult();
                if (usos > 0) {
                    throw new ReglaNegocioException("El apartado ya fue convertido en otra venta.");
                }
                if (venta.getDetalles().size() != 1
                        || !venta.getDetalles().getFirst().getPieza().getId()
                        .equals(apartado.getPieza().getId())
                        || !venta.getDetalles().getFirst().getCantidad().equals(apartado.getCantidad())
                        || !venta.getCliente().getId().equals(apartado.getCliente().getId())) {
                    throw new ReglaNegocioException(
                            "La venta debe coincidir con la pieza y cantidad reservadas en el apartado.");
                }
            }

            for (DetalleVenta detalle : venta.getDetalles()) {
                validarDetalleGuardado(detalle);
                if (!inventarioYaReservado) {
                    Pieza pieza = em.find(
                            Pieza.class, detalle.getPieza().getId(), LockModeType.PESSIMISTIC_WRITE);
                    if (pieza.getExistencias() < detalle.getCantidad()) {
                        throw new ReglaNegocioException(
                                "Stock insuficiente para " + pieza.getNombre()
                                        + ". Disponible: " + pieza.getExistencias() + ".");
                    }
                    pieza.setExistencias(pieza.getExistencias() - detalle.getCantidad());
                }
            }

            venta.setTotal(calcularTotal(venta));
            if (apartado != null) {
                apartado.setEstado(EstadoApartado.COMPLETADO);
                venta.setApartadoOrigen(apartado);
            }
            venta.setInventarioComprometido(true);
            venta.setEstado(PoliticaEstadoVenta.confirmar(venta.getEstado()));
            return venta;
        });
    }

    private void cargarDetalles(EntityManager em, Venta venta,
                                 List<SolicitudDetalleVenta> solicitudes) {
        if (solicitudes == null || solicitudes.isEmpty()) {
            throw new ReglaNegocioException("La venta debe contener al menos una pieza.");
        }

        Set<Long> piezasIncluidas = new HashSet<>();
        for (SolicitudDetalleVenta solicitud : solicitudes) {
            if (solicitud == null || solicitud.piezaId() == null) {
                throw new ReglaNegocioException("Cada detalle debe indicar una pieza.");
            }
            if (!piezasIncluidas.add(solicitud.piezaId())) {
                throw new ReglaNegocioException(
                        "Una pieza no puede aparecer dos veces; modifica su cantidad.");
            }

            Pieza pieza = em.find(Pieza.class, solicitud.piezaId());
            if (pieza == null) {
                throw new ReglaNegocioException("No existe la pieza " + solicitud.piezaId() + ".");
            }
            CalculadoraVenta.validarCantidad(solicitud.cantidad());
            BigDecimal descuento = solicitud.descuentoUnitario() == null
                    ? BigDecimal.ZERO : solicitud.descuentoUnitario();
            BigDecimal precio = BigDecimal.valueOf(pieza.getPrecioVenta());
            BigDecimal costo = BigDecimal.valueOf(pieza.getCosto());
            CalculadoraVenta.calcularSubtotal(precio, costo, descuento, solicitud.cantidad());

            venta.agregarDetalle(new DetalleVenta(
                    pieza,
                    solicitud.cantidad(),
                    precio,
                    costo,
                    descuento.setScale(CalculadoraVenta.DECIMALES_MONEDA, RoundingMode.HALF_UP)
            ));
        }
        venta.setTotal(calcularTotal(venta));
    }

    private BigDecimal calcularTotal(Venta venta) {
        return CalculadoraVenta.calcularTotal(
                venta.getDetalles().stream().map(DetalleVenta::getSubtotal).toList());
    }

    private void validarDetalleGuardado(DetalleVenta detalle) {
        CalculadoraVenta.calcularSubtotal(
                detalle.getPrecioUnitario(),
                detalle.getCostoUnitarioVenta(),
                detalle.getDescuentoAplicado(),
                detalle.getCantidad());
    }

    private Cliente requerirCliente(EntityManager em, Long clienteId) {
        if (clienteId == null) {
            throw new ReglaNegocioException("Debe seleccionar un cliente.");
        }
        Cliente cliente = em.find(Cliente.class, clienteId);
        if (cliente == null) {
            throw new ReglaNegocioException("No existe el cliente " + clienteId + ".");
        }
        return cliente;
    }

    private String validarMetodoPago(String metodoPago) {
        if (metodoPago == null) {
            throw new ReglaNegocioException("Debe seleccionar un metodo de pago.");
        }
        String normalizado = metodoPago.trim().toUpperCase();
        if (!Set.of("EFECTIVO", "TARJETA", "TRANSFERENCIA").contains(normalizado)) {
            throw new ReglaNegocioException("Metodo de pago no valido.");
        }
        return normalizado;
    }

    private Venta requerirVenta(EntityManager em, Long ventaId, LockModeType lockMode) {
        if (ventaId == null) {
            throw new ReglaNegocioException("Debe seleccionar una venta.");
        }
        Venta venta = em.find(Venta.class, ventaId, lockMode);
        if (venta == null) {
            throw new ReglaNegocioException("No existe la venta " + ventaId + ".");
        }
        venta.getDetalles().size();
        return venta;
    }

    private <T> T enTransaccion(Function<EntityManager, T> operacion) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T resultado = operacion.apply(em);
            tx.commit();
            return resultado;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
