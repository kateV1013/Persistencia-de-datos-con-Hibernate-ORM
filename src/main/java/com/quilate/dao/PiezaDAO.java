package com.quilate.dao;

import com.quilate.exception.EntidadConDependenciasException;
import com.quilate.model.Categoria;
import com.quilate.model.Pieza;
import com.quilate.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.hibernate.exception.ConstraintViolationException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO (Data Access Object) para la entidad {@link Pieza}.
 *
 * <p>Maneja todas las operaciones de persistencia de piezas usando
 * la API de {@link EntityManager} y transacciones manuales.</p>
 *
 * <p><b>Reglas de negocio:</b>
 * <ul>
 *   <li>El {@code codigoBarras} debe ser único.</li>
 *   <li>Las existencias no pueden ser negativas.</li>
 *   <li>No se puede eliminar una pieza que esté referenciada en otras tablas
 *       (p. ej. DetalleVenta – Integrante 3, Apartado – Integrante 2).
 *       La violación de FK se captura y convierte en
 *       {@link EntidadConDependenciasException}.</li>
 * </ul>
 * </p>
 */
public class PiezaDAO {

    private static final Logger LOG = Logger.getLogger(PiezaDAO.class.getName());

    // ──────────────────────────────────────────────────────────────────────
    //  CREATE
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Persiste una nueva {@link Pieza} en la base de datos.
     *
     * @param pieza la pieza a guardar (sin ID aún)
     * @return la pieza con su ID asignado por la BD
     * @throws IllegalArgumentException si el código de barras ya existe
     *                                  o las existencias son negativas
     */
    public Pieza guardar(Pieza pieza) {
        validarPieza(pieza);

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Si la categoría viene detached, la volvemos a adjuntar
            Categoria categoriaManaged = em.find(Categoria.class, pieza.getCategoria().getId());
            if (categoriaManaged == null) {
                throw new IllegalArgumentException(
                        "La categoría indicada no existe en la BD.");
            }
            pieza.setCategoria(categoriaManaged);

            em.persist(pieza);
            tx.commit();
            LOG.info("Pieza guardada: " + pieza.getCodigoBarras());
            return pieza;

        } catch (IllegalArgumentException e) {
            if (tx.isActive()) tx.rollback();
            throw e;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (esDuplicado(e)) {
                throw new IllegalArgumentException(
                        "Ya existe una pieza con el código de barras: " + pieza.getCodigoBarras(), e);
            }
            LOG.log(Level.SEVERE, "Error al guardar pieza", e);
            throw new RuntimeException("Error al guardar la pieza.", e);

        } finally {
            em.close();
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  READ
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Busca una pieza por su ID.
     *
     * @param id identificador de la pieza
     * @return {@link Optional} con la pieza, o vacío si no existe
     */
    public Optional<Pieza> buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // JOIN FETCH para cargar la categoría en la misma consulta
            TypedQuery<Pieza> q = em.createQuery(
                    "SELECT p FROM Pieza p JOIN FETCH p.categoria WHERE p.id = :id",
                    Pieza.class);
            q.setParameter("id", id);
            Pieza pieza = q.getSingleResult();
            return Optional.of(pieza);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Busca una pieza por su código de barras exacto.
     * <p>Este método es el punto de entrada del lector físico de barras.</p>
     *
     * @param codigoBarras código escaneado por el lector
     * @return {@link Optional} con la pieza, o vacío si no existe
     */
    public Optional<Pieza> buscarPorCodigoBarras(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.isBlank()) {
            return Optional.empty();
        }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Pieza> q = em.createQuery(
                    "SELECT p FROM Pieza p JOIN FETCH p.categoria " +
                    "WHERE p.codigoBarras = :codigo",
                    Pieza.class);
            q.setParameter("codigo", codigoBarras.trim());
            Pieza pieza = q.getSingleResult();
            return Optional.of(pieza);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Retorna todas las piezas ordenadas por nombre, con su categoría cargada.
     *
     * @return lista de piezas (puede ser vacía, nunca null)
     */
    public List<Pieza> listarTodas() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Pieza p JOIN FETCH p.categoria ORDER BY p.nombre",
                    Pieza.class).getResultList();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al listar piezas", e);
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    /**
     * Lista todas las piezas de una categoría específica.
     *
     * @param categoriaId ID de la categoría
     * @return lista de piezas de esa categoría
     */
    public List<Pieza> listarPorCategoria(Long categoriaId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Pieza> q = em.createQuery(
                    "SELECT p FROM Pieza p JOIN FETCH p.categoria c " +
                    "WHERE c.id = :catId ORDER BY p.nombre",
                    Pieza.class);
            q.setParameter("catId", categoriaId);
            return q.getResultList();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al listar piezas por categoría", e);
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    /**
     * Busca piezas cuyo nombre contenga el texto dado (búsqueda parcial, case-insensitive).
     *
     * @param textoBusqueda fragmento del nombre a buscar
     * @return lista de piezas que coinciden
     */
    public List<Pieza> buscarPorNombre(String textoBusqueda) {
        if (textoBusqueda == null || textoBusqueda.isBlank()) {
            return listarTodas();
        }
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Pieza> q = em.createQuery(
                    "SELECT p FROM Pieza p JOIN FETCH p.categoria " +
                    "WHERE LOWER(p.nombre) LIKE LOWER(:texto) ORDER BY p.nombre",
                    Pieza.class);
            q.setParameter("texto", "%" + textoBusqueda.trim() + "%");
            return q.getResultList();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error en búsqueda por nombre", e);
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    /**
     * Verifica si ya existe una pieza con el código de barras dado.
     * Útil para validación en el controlador antes de intentar guardar.
     *
     * @param codigoBarras código a verificar
     * @param idExcluir    ID de la pieza actual (para edición), null si es alta nueva
     * @return {@code true} si el código ya está en uso por otra pieza
     */
    public boolean existeCodigoBarras(String codigoBarras, Long idExcluir) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = (idExcluir == null)
                    ? "SELECT COUNT(p) FROM Pieza p WHERE p.codigoBarras = :codigo"
                    : "SELECT COUNT(p) FROM Pieza p WHERE p.codigoBarras = :codigo AND p.id <> :excluir";

            var query = em.createQuery(jpql, Long.class)
                    .setParameter("codigo", codigoBarras);
            if (idExcluir != null) {
                query.setParameter("excluir", idExcluir);
            }
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  UPDATE
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Actualiza los datos de una pieza existente.
     *
     * @param pieza pieza con los datos modificados (debe tener ID)
     * @return la pieza actualizada y gestionada
     */
    public Pieza actualizar(Pieza pieza) {
        validarPieza(pieza);

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            Pieza actualizada = em.merge(pieza);
            tx.commit();
            LOG.info("Pieza actualizada: ID=" + pieza.getId());
            return actualizada;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (esDuplicado(e)) {
                throw new IllegalArgumentException(
                        "Ya existe una pieza con el código de barras: " + pieza.getCodigoBarras(), e);
            }
            LOG.log(Level.SEVERE, "Error al actualizar pieza", e);
            throw new RuntimeException("Error al actualizar la pieza.", e);

        } finally {
            em.close();
        }
    }

    /**
     * Actualiza únicamente las existencias de una pieza.
     * Útil para ajustes de inventario rápidos.
     *
     * @param id           ID de la pieza
     * @param nuevasExistencias nuevo valor de existencias (>= 0)
     */
    public void actualizarExistencias(Long id, int nuevasExistencias) {
        if (nuevasExistencias < 0) {
            throw new IllegalArgumentException("Las existencias no pueden ser negativas.");
        }

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            int filas = em.createQuery(
                    "UPDATE Pieza p SET p.existencias = :exist WHERE p.id = :id")
                    .setParameter("exist", nuevasExistencias)
                    .setParameter("id", id)
                    .executeUpdate();
            if (filas == 0) {
                throw new IllegalArgumentException("No se encontró pieza con ID: " + id);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  DELETE
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Elimina una pieza por ID.
     *
     * <p><b>Regla de negocio:</b> No se puede eliminar si la pieza está
     * referenciada en otras tablas (p. ej. DetalleVenta, Apartado).
     * La violación de FK se convierte en {@link EntidadConDependenciasException}.</p>
     *
     * <p>⚠️ La verificación explícita de dependencias la harán
     * los Integrantes 2 y 3 cuando integren sus entidades.
     * Por ahora, dependemos de la FK de la BD como segunda línea de defensa.</p>
     *
     * @param id ID de la pieza a eliminar
     * @throws EntidadConDependenciasException si la pieza tiene registros dependientes
     * @throws IllegalArgumentException        si no existe la pieza
     */
    public void eliminar(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Pieza pieza = em.find(Pieza.class, id);
            if (pieza == null) {
                throw new IllegalArgumentException("No se encontró la pieza con ID: " + id);
            }

            /*
             * TODO: Integrante 2 (Apartado) y Integrante 3 (DetalleVenta)
             * deberán agregar aquí las consultas de verificación cuando sus
             * entidades estén disponibles. Ejemplo:
             *
             *   long enApartados = (long) em.createQuery(
             *       "SELECT COUNT(a) FROM Apartado a JOIN a.piezas p WHERE p.id = :id")
             *       .setParameter("id", id).getSingleResult();
             *   if (enApartados > 0) { lanzar EntidadConDependenciasException }
             *
             *   long enVentas = (long) em.createQuery(
             *       "SELECT COUNT(dv) FROM DetalleVenta dv WHERE dv.pieza.id = :id")
             *       .setParameter("id", id).getSingleResult();
             *   if (enVentas > 0) { lanzar EntidadConDependenciasException }
             */

            em.remove(pieza);
            tx.commit();
            LOG.info("Pieza eliminada: ID=" + id + ", código=" + pieza.getCodigoBarras());

        } catch (IllegalArgumentException e) {
            if (tx.isActive()) tx.rollback();
            throw e;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            // La FK de BD rechazó el DELETE (segunda línea de defensa)
            if (esFKViolation(e)) {
                throw new EntidadConDependenciasException(
                        "No se puede eliminar la pieza porque está referenciada " +
                        "en ventas, apartados u otros registros.", e);
            }
            LOG.log(Level.SEVERE, "Error al eliminar pieza ID=" + id, e);
            throw new RuntimeException("Error al eliminar la pieza.", e);

        } finally {
            em.close();
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Helpers privados
    // ──────────────────────────────────────────────────────────────────────

    private void validarPieza(Pieza pieza) {
        if (pieza.getCodigoBarras() == null || pieza.getCodigoBarras().isBlank()) {
            throw new IllegalArgumentException("El código de barras es obligatorio.");
        }
        if (pieza.getNombre() == null || pieza.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la pieza es obligatorio.");
        }
        if (pieza.getCosto() == null || pieza.getCosto() < 0) {
            throw new IllegalArgumentException("El costo debe ser un valor no negativo.");
        }
        if (pieza.getPrecioVenta() == null || pieza.getPrecioVenta() < 0) {
            throw new IllegalArgumentException("El precio de venta debe ser un valor no negativo.");
        }
        if (pieza.getExistencias() == null || pieza.getExistencias() < 0) {
            throw new IllegalArgumentException("Las existencias no pueden ser negativas.");
        }
        if (pieza.getCategoria() == null || pieza.getCategoria().getId() == null) {
            throw new IllegalArgumentException("La pieza debe tener una categoría asignada.");
        }
    }

    private boolean esDuplicado(Exception e) {
        Throwable causa = e;
        while (causa != null) {
            if (causa instanceof ConstraintViolationException cve) {
                String msg = cve.getMessage() != null ? cve.getMessage().toLowerCase() : "";
                return msg.contains("unique") || msg.contains("uk_pieza_codigo_barras");
            }
            causa = causa.getCause();
        }
        return false;
    }

    private boolean esFKViolation(Exception e) {
        Throwable causa = e;
        while (causa != null) {
            if (causa instanceof ConstraintViolationException cve) {
                String msg = cve.getMessage() != null ? cve.getMessage().toLowerCase() : "";
                return msg.contains("foreign key") || msg.contains("fk_");
            }
            causa = causa.getCause();
        }
        return false;
    }
}
