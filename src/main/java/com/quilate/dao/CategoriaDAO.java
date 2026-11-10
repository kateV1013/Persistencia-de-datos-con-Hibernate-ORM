package com.quilate.dao;

import com.quilate.exception.EntidadConDependenciasException;
import com.quilate.model.Categoria;
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
 * DAO (Data Access Object) para la entidad {@link Categoria}.
 *
 * <p>Maneja todas las operaciones de persistencia de categorías usando
 * la API de {@link EntityManager} y transacciones manuales.</p>
 *
 * <p><b>Regla de negocio:</b> No se puede eliminar una categoría que ya
 * tenga piezas asociadas. Si se intenta, se lanza
 * {@link EntidadConDependenciasException}.</p>
 */
public class CategoriaDAO {

    private static final Logger LOG = Logger.getLogger(CategoriaDAO.class.getName());

    // ──────────────────────────────────────────────────────────────────────
    //  CREATE
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Persiste una nueva {@link Categoria} en la base de datos.
     *
     * @param categoria la categoría a guardar (sin ID aún)
     * @return la categoría con su ID asignado por la BD
     * @throws IllegalArgumentException si el nombre está vacío o ya existe
     */
    public Categoria guardar(Categoria categoria) {
        validarNombre(categoria.getNombre());

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(categoria);
            tx.commit();
            LOG.info("Categoría guardada: " + categoria.getNombre());
            return categoria;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (esDuplicado(e)) {
                throw new IllegalArgumentException(
                        "Ya existe una categoría con el nombre: " + categoria.getNombre(), e);
            }
            LOG.log(Level.SEVERE, "Error al guardar categoría", e);
            throw new RuntimeException("Error al guardar la categoría.", e);

        } finally {
            em.close();
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  READ
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Busca una categoría por su ID.
     *
     * @param id identificador de la categoría
     * @return {@link Optional} con la categoría, o vacío si no existe
     */
    public Optional<Categoria> buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Categoria categoria = em.find(Categoria.class, id);
            return Optional.ofNullable(categoria);
        } finally {
            em.close();
        }
    }

    /**
     * Busca una categoría por su nombre exacto (case-insensitive).
     *
     * @param nombre nombre a buscar
     * @return {@link Optional} con la categoría, o vacío si no existe
     */
    public Optional<Categoria> buscarPorNombre(String nombre) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Categoria> query = em.createQuery(
                    "SELECT c FROM Categoria c WHERE LOWER(c.nombre) = LOWER(:nombre)",
                    Categoria.class);
            query.setParameter("nombre", nombre);
            Categoria resultado = query.getSingleResult();
            return Optional.of(resultado);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Retorna todas las categorías ordenadas por nombre.
     *
     * @return lista de categorías (puede ser vacía, nunca null)
     */
    public List<Categoria> listarTodas() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM Categoria c ORDER BY c.nombre",
                    Categoria.class).getResultList();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al listar categorías", e);
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  UPDATE
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Actualiza los datos de una categoría existente.
     *
     * @param categoria categoría con los datos modificados (debe tener ID)
     * @return la categoría actualizada y gestionada
     * @throws IllegalArgumentException si el nuevo nombre ya existe en otra categoría
     */
    public Categoria actualizar(Categoria categoria) {
        validarNombre(categoria.getNombre());

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            Categoria actualizada = em.merge(categoria);
            tx.commit();
            LOG.info("Categoría actualizada: ID=" + categoria.getId());
            return actualizada;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (esDuplicado(e)) {
                throw new IllegalArgumentException(
                        "Ya existe una categoría con el nombre: " + categoria.getNombre(), e);
            }
            LOG.log(Level.SEVERE, "Error al actualizar categoría", e);
            throw new RuntimeException("Error al actualizar la categoría.", e);

        } finally {
            em.close();
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  DELETE
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Elimina una categoría por ID.
     *
     * <p><b>Regla de negocio:</b> Si la categoría tiene piezas asociadas,
     * la FK de BD rechazará la operación. Capturamos esa excepción y lanzamos
     * {@link EntidadConDependenciasException} con un mensaje claro.</p>
     *
     * @param id ID de la categoría a eliminar
     * @throws EntidadConDependenciasException si la categoría tiene piezas
     * @throws IllegalArgumentException        si no se encuentra la categoría
     */
    public void eliminar(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Categoria categoria = em.find(Categoria.class, id);
            if (categoria == null) {
                throw new IllegalArgumentException("No se encontró la categoría con ID: " + id);
            }

            // Verificación explícita ANTES de intentar el DELETE en BD.
            // Esto da un mensaje amigable sin depender del error de FK.
            long piezasAsociadas = (long) em.createQuery(
                    "SELECT COUNT(p) FROM Pieza p WHERE p.categoria.id = :catId")
                    .setParameter("catId", id)
                    .getSingleResult();

            if (piezasAsociadas > 0) {
                tx.rollback();
                throw new EntidadConDependenciasException(
                        "No se puede eliminar la categoría '" + categoria.getNombre() +
                        "' porque tiene " + piezasAsociadas + " pieza(s) asociada(s). " +
                        "Reasigna o elimina las piezas primero.");
            }

            em.remove(categoria);
            tx.commit();
            LOG.info("Categoría eliminada: ID=" + id);

        } catch (EntidadConDependenciasException | IllegalArgumentException e) {
            if (tx.isActive()) tx.rollback();
            throw e; // relanzar sin envolver

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            // Por si la FK falla a nivel de BD (segunda línea de defensa)
            if (esFKViolation(e)) {
                throw new EntidadConDependenciasException(
                        "No se puede eliminar la categoría porque tiene registros dependientes.", e);
            }
            LOG.log(Level.SEVERE, "Error al eliminar categoría ID=" + id, e);
            throw new RuntimeException("Error al eliminar la categoría.", e);

        } finally {
            em.close();
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Helpers privados
    // ──────────────────────────────────────────────────────────────────────

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
    }

    /** Detecta si la excepción es por violación de UNIQUE constraint. */
    private boolean esDuplicado(Exception e) {
        Throwable causa = e;
        while (causa != null) {
            if (causa instanceof ConstraintViolationException cve) {
                String msg = cve.getMessage() != null ? cve.getMessage().toLowerCase() : "";
                return msg.contains("unique") || msg.contains("uk_categoria_nombre");
            }
            causa = causa.getCause();
        }
        return false;
    }

    /** Detecta si la excepción es por violación de FOREIGN KEY constraint. */
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
