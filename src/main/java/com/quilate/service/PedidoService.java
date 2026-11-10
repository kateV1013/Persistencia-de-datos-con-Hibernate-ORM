package com.quilate.service;

import com.quilate.dao.PedidoPersonalizadoDAO;
import com.quilate.exception.ReglaNegocioException;
import com.quilate.model.Cliente;
import com.quilate.model.EstadoPedido;
import com.quilate.model.PedidoPersonalizado;
import com.quilate.model.TipoServicio;
import com.quilate.util.Transaccion;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Registro, consulta, modificacion y cancelacion de pedidos personalizados.
 * Regla: maximo {@value #CUPO_SEMANAL} pedidos activos por semana (lunes a domingo).
 */
public class PedidoService {

    public static final int CUPO_SEMANAL = 20;

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PedidoPersonalizadoDAO dao = new PedidoPersonalizadoDAO();

    // ---------- consultas ----------

    public List<PedidoPersonalizado> listar(boolean incluirCancelados) {
        return dao.listarConDetalle(incluirCancelados);
    }

    /** Pedidos activos que ya hay en la semana (lunes a domingo) de la fecha indicada. */
    public long ocupadosEnSemana(LocalDate fecha) {
        return dao.contarActivosEntre(lunesDe(fecha), domingoDe(fecha));
    }

    // ---------- operaciones ----------

    public PedidoPersonalizado registrar(PedidoPersonalizado pedido) {
        validarCampos(pedido);
        if (pedido.getFechaProgramada().isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha programada no puede ser anterior a hoy.");
        }
        pedido.setId(null);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDescripcionTrabajo(pedido.getDescripcionTrabajo().trim());

        return Transaccion.ejecutar(em -> {
            verificarCupo(em, pedido.getFechaProgramada(), null);
            pedido.setCliente(em.getReference(Cliente.class, pedido.getCliente().getId()));
            pedido.setTipoServicio(em.getReference(TipoServicio.class, pedido.getTipoServicio().getId()));
            em.persist(pedido);
            return pedido;
        });
    }

    /**
     * Modifica un pedido existente. Si cambia la fecha, se vuelve a revisar el cupo
     * de la nueva semana sin contar el propio pedido.
     */
    public PedidoPersonalizado modificar(PedidoPersonalizado datos) {
        if (datos.getId() == null) {
            throw new ReglaNegocioException("Selecciona el pedido que deseas modificar.");
        }
        validarCampos(datos);
        if (datos.getEstado() == null) {
            throw new ReglaNegocioException("Selecciona el estado del pedido.");
        }
        if (datos.getEstado() == EstadoPedido.CANCELADO) {
            throw new ReglaNegocioException("Para cancelar un pedido usa el boton \"Cancelar pedido\".");
        }

        return Transaccion.ejecutar(em -> {
            PedidoPersonalizado actual = em.find(PedidoPersonalizado.class, datos.getId());
            if (actual == null) {
                throw new ReglaNegocioException("El pedido ya no existe.");
            }
            if (actual.getEstado() == EstadoPedido.CANCELADO) {
                throw new ReglaNegocioException("Un pedido cancelado no se puede modificar.");
            }

            boolean cambioFecha = !actual.getFechaProgramada().equals(datos.getFechaProgramada());
            if (cambioFecha) {
                if (datos.getFechaProgramada().isBefore(LocalDate.now())) {
                    throw new ReglaNegocioException("La nueva fecha no puede ser anterior a hoy.");
                }
                verificarCupo(em, datos.getFechaProgramada(), actual.getId());
            }

            actual.setCliente(em.getReference(Cliente.class, datos.getCliente().getId()));
            actual.setTipoServicio(em.getReference(TipoServicio.class, datos.getTipoServicio().getId()));
            actual.setDescripcionTrabajo(datos.getDescripcionTrabajo().trim());
            actual.setFechaProgramada(datos.getFechaProgramada());
            actual.setPrecio(datos.getPrecio());
            actual.setEstado(datos.getEstado());
            return actual;
        });
    }

    /** Cancela el pedido (no se borra: queda en el historial y libera su cupo). */
    public void cancelar(Integer id) {
        Transaccion.ejecutar(em -> {
            PedidoPersonalizado pedido = em.find(PedidoPersonalizado.class, id);
            if (pedido == null) {
                throw new ReglaNegocioException("El pedido ya no existe.");
            }
            if (pedido.getEstado() == EstadoPedido.CANCELADO) {
                throw new ReglaNegocioException("El pedido ya estaba cancelado.");
            }
            if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
                throw new ReglaNegocioException("No se puede cancelar un pedido ya entregado.");
            }
            pedido.setEstado(EstadoPedido.CANCELADO);
            return null;
        });
    }

    // ---------- reglas ----------

    private void verificarCupo(EntityManager em, LocalDate fecha, Integer excluirId) {
        LocalDate lunes = lunesDe(fecha);
        LocalDate domingo = domingoDe(fecha);
        long ocupados = dao.contarActivosEntre(em, lunes, domingo, excluirId);
        if (ocupados >= CUPO_SEMANAL) {
            throw new ReglaNegocioException("Ya no hay espacio en la semana del "
                    + lunes.format(FECHA) + " al " + domingo.format(FECHA)
                    + " (" + ocupados + "/" + CUPO_SEMANAL + " pedidos). Elige otra fecha.");
        }
    }

    private void validarCampos(PedidoPersonalizado p) {
        if (p.getCliente() == null || p.getCliente().getId() == null) {
            throw new ReglaNegocioException("Selecciona el cliente.");
        }
        if (p.getTipoServicio() == null || p.getTipoServicio().getId() == null) {
            throw new ReglaNegocioException("Selecciona el tipo de servicio.");
        }
        String descripcion = p.getDescripcionTrabajo();
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaNegocioException("Describe el trabajo a realizar.");
        }
        if (descripcion.trim().length() > 500) {
            throw new ReglaNegocioException("La descripcion no puede tener mas de 500 caracteres.");
        }
        if (p.getFechaProgramada() == null) {
            throw new ReglaNegocioException("Selecciona la fecha programada.");
        }
        BigDecimal precio = p.getPrecio();
        if (precio == null) {
            throw new ReglaNegocioException("El precio es obligatorio.");
        }
        if (precio.signum() < 0) {
            throw new ReglaNegocioException("El precio no puede ser negativo.");
        }
    }

    public static LocalDate lunesDe(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate domingoDe(LocalDate fecha) {
        return lunesDe(fecha).plusDays(6);
    }
}
