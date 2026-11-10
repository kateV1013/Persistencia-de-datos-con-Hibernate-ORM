package com.quilate.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa una Categoría de piezas de joyería.
 *
 * <p>Relación: Una {@code Categoria} contiene muchas {@link Pieza}s
 * ({@code @OneToMany}).</p>
 *
 * <p>Restricciones de negocio:
 * <ul>
 *   <li>{@code nombre} es único y obligatorio.</li>
 *   <li>No se puede eliminar una categoría que tenga piezas asociadas
 *       (ver {@link com.quilate.dao.CategoriaDAO#eliminar(Long)}).</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "categorias",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_categoria_nombre",
               columnNames = "nombre"))
@Data
@NoArgsConstructor
@AllArgsConstructor
// Excluimos la colección de piezas del toString para evitar lazy-load al loggear
@ToString(exclude = "piezas")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Nombre de la categoría. Único y no puede ser nulo. */
    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    /** Descripción opcional de la categoría. */
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    /**
     * Lista de piezas que pertenecen a esta categoría.
     *
     * <ul>
     *   <li>{@code fetch = LAZY}: no carga las piezas hasta que se acceda a ellas.</li>
     *   <li>{@code cascade = PERSIST, MERGE}: si persistes/actualizas la categoría
     *       con piezas nuevas en la lista, también las persiste.</li>
     *   <li>NO se pone {@code CascadeType.REMOVE} a propósito: si alguien intenta
     *       borrar una categoría con piezas, la FK de BD lanzará una excepción
     *       que atrapamos en el DAO.</li>
     * </ul>
     */
    @OneToMany(
            mappedBy = "categoria",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Pieza> piezas = new ArrayList<>();

    // ── Constructor de conveniencia (sin colección) ───────────────────────

    /**
     * Crea una categoría con nombre y descripción, sin piezas aún.
     * Útil para formularios de alta.
     */
    public Categoria(String nombre, String descripcion) {
        this.nombre      = nombre;
        this.descripcion = descripcion;
        this.piezas      = new ArrayList<>();
    }
}
