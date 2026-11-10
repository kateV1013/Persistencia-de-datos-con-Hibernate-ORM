package com.quilate.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * Entidad JPA que representa una Pieza del inventario de la joyería.
 *
 * <p>Restricciones de negocio:
 * <ul>
 *   <li>{@code codigoBarras} es único y obligatorio.</li>
 *   <li>{@code nombre}, {@code costo}, {@code precioVenta} y
 *       {@code existencias} son obligatorios.</li>
 *   <li>{@code existencias} no puede ser negativo (validado en el DAO/Controller).</li>
 *   <li>Cada pieza pertenece a exactamente una {@link Categoria} ({@code @ManyToOne}).</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "piezas",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_pieza_codigo_barras",
               columnNames = "codigo_barras"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "categoria")   // evitar loop en toString con Categoria
public class Pieza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Código de barras de la pieza. Único en toda la tabla.
     * El lector físico de barras lo ingresa como texto.
     */
    @Column(name = "codigo_barras", nullable = false, unique = true, length = 50)
    private String codigoBarras;

    /** Nombre descriptivo de la pieza. */
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    /** Costo de adquisición de la pieza (en la moneda del negocio). */
    @Column(name = "costo", nullable = false)
    private Double costo;

    /** Precio de venta al público. */
    @Column(name = "precio_venta", nullable = false)
    private Double precioVenta;

    /**
     * Cantidad en existencia en inventario físico.
     * Nunca debe ser negativo.
     */
    @Column(name = "existencias", nullable = false)
    private Integer existencias;

    /**
     * Categoría a la que pertenece esta pieza.
     *
     * <ul>
     *   <li>{@code fetch = LAZY}: carga la categoría solo cuando se accede.</li>
     *   <li>{@code optional = false}: toda pieza DEBE tener categoría.</li>
     * </ul>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "categoria_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pieza_categoria"))
    private Categoria categoria;

    // ── Constructor de conveniencia (sin ID, para altas) ─────────────────

    /**
     * Crea una pieza nueva (sin ID) lista para persistir.
     * El {@code id} lo asigna Hibernate tras el {@code persist()}.
     */
    public Pieza(String codigoBarras, String nombre,
                 Double costo, Double precioVenta,
                 Integer existencias, Categoria categoria) {
        this.codigoBarras = codigoBarras;
        this.nombre       = nombre;
        this.costo        = costo;
        this.precioVenta  = precioVenta;
        this.existencias  = existencias;
        this.categoria    = categoria;
    }
}
