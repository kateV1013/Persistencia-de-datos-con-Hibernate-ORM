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
    /** Un pedido se programa como máximo con dos meses de anticipación. */
    public static final int MESES_MAXIMOS_DE_ANTICIPACION = 2;
    /** Límite de la columna NUMERIC(10,2) de precio. */
    public static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");

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
        validarAnticipacion(pedido.getFechaProgramada());
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
            throw new ReglaNegocioException("Para cancelar un pedido usa el botón \"Cancelar pedido\".");
        }

        return Transaccion.ejecutar(em -> {
            PedidoPersonalizado actual = em.find(PedidoPersonalizado.class, datos.getId());
            if (actual == null) {
                throw new ReglaNegocioException("El pedido ya no existe.");
            }
            if (actual.getEstado() == EstadoPedido.CANCELADO) {
                throw new ReglaNegocioException("Un pedido cancelado no se puede modificar.");
            }
            if (actual.getEstado() == EstadoPedido.ENTREGADO) {
                throw new ReglaNegocioException("Un pedido entregado ya no se puede modificar.");
            }
            validarTransicion(actual.getEstado(), datos.getEstado());
            if (datos.getEstado() == EstadoPedido.ENTREGADO
                    && datos.getFechaProgramada().isAfter(LocalDate.now())) {
                throw new ReglaNegocioException("No se puede marcar como entregado un pedido programado para el "
                        + datos.getFechaProgramada().format(FECHA) + ": su fecha aún no llega.");
            }

            boolean cambioFecha = !actual.getFechaProgramada().equals(datos.getFechaProgramada());
            if (cambioFecha) {
                if (datos.getFechaProgramada().isBefore(LocalDate.now())) {
                    throw new ReglaNegocioException("La nueva fecha no puede ser anterior a hoy.");
                }
                validarAnticipacion(datos.getFechaProgramada());
                verificarCupo(em, datos.getFechaProgramada(), actual.getId());
            }

            actual.setCliente(em.getReference(Cliente.class, datos.getCliente().getId()));
            actual.setTipoServicio(em.getReference(TipoServicio.class, datos.getTipoServicio().getId()));
            actual.setDescripcionTrabajo(datos.getDescripcionTrabajo().trim());
            actual.setFechaProgramada(datos.getFechaProgramada());
            actual.setPrecio(datos.getPrecio());
            actual.setEstado(datos.getEstado());
            actual.setDiseno(datos.getDiseno()==null ? null : datos.getDiseno().copia());
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

    /**
     * Un pedido avanza Pendiente → En proceso → Terminado → Entregado.
     * Puede quedarse en su estado o avanzar (incluso saltar pasos), pero no regresar.
     */
    static void validarTransicion(EstadoPedido actual, EstadoPedido nuevo) {
        if (nuevo.ordinal() < actual.ordinal()) {
            throw new ReglaNegocioException("Un pedido no puede regresar de \""
                    + actual + "\" a \"" + nuevo + "\".");
        }
    }

    private void validarCampos(PedidoPersonalizado p) {
        DisenoService.validar(p.getDiseno());
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
            throw new ReglaNegocioException("La descripción no puede tener más de 500 caracteres.");
        }
        if (p.getFechaProgramada() == null) {
            throw new ReglaNegocioException("Selecciona la fecha programada.");
        }
        BigDecimal precio = p.getPrecio();
        if (precio == null) {
            throw new ReglaNegocioException("El precio es obligatorio.");
        }
        if (precio.signum() <= 0) {
            throw new ReglaNegocioException("El precio debe ser mayor que cero.");
        }
        if (precio.compareTo(PRECIO_MAXIMO) > 0) {
            throw new ReglaNegocioException("El precio no puede pasar de C$ 99,999,999.99.");
        }
        p.setPrecio(precio.setScale(2, java.math.RoundingMode.HALF_UP));
    }

    /** Evita fechas sin sentido como el año 9999: máximo dos meses adelante. */
    private static void validarAnticipacion(LocalDate fecha) {
        LocalDate limite = LocalDate.now().plusMonths(MESES_MAXIMOS_DE_ANTICIPACION);
        if (fecha.isAfter(limite)) {
            throw new ReglaNegocioException("La fecha programada no puede pasar del "
                    + limite.format(FECHA) + " (dos meses desde hoy).");
        }
    }

    public static LocalDate lunesDe(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate domingoDe(LocalDate fecha) {
        return lunesDe(fecha).plusDays(6);
    }
}
