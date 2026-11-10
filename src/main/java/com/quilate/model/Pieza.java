package com.quilate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(name = "piezas", uniqueConstraints = @UniqueConstraint(
        name = "uk_pieza_codigo_barras", columnNames = "codigo_barras"))
public class Pieza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_barras", nullable = false, unique = true, length = 50)
    private String codigoBarras;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 50)
    private String material;

    private Short quilates;

    @Column(name = "peso_gramos", precision = 8, scale = 2)
    private BigDecimal pesoGramos;

    @Column(nullable = false)
    private Double costo;

    @Column(name = "precio_venta", nullable = false)
    private Double precioVenta;

    @Column(nullable = false)
    private Integer existencias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", foreignKey = @ForeignKey(name = "fk_pieza_categoria"))
    private Categoria categoria;

    @jakarta.persistence.OneToMany(mappedBy = "pieza")
    private java.util.List<Apartado> apartados = new java.util.ArrayList<>();

    public java.util.List<Apartado> getApartados() { return apartados; }

    public Pieza() {
    }

    public Pieza(String codigoBarras, String nombre, Double costo, Double precioVenta,
                 Integer existencias, Categoria categoria) {
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.costo = costo;
        this.precioVenta = precioVenta;
        this.existencias = existencias;
        this.categoria = categoria;
    }

    /** Constructor compatible con los datos y pruebas del modulo de reportes. */
    public Pieza(String codigoBarras, String nombre, String material, BigDecimal costo,
                 BigDecimal precioVenta, String categoria, int cantidadDisponible) {
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.material = material;
        this.costo = costo.doubleValue();
        this.precioVenta = precioVenta.doubleValue();
        this.existencias = cantidadDisponible;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public Short getQuilates() { return quilates; }
    public void setQuilates(Short quilates) { this.quilates = quilates; }
    public BigDecimal getPesoGramos() { return pesoGramos; }
    public void setPesoGramos(BigDecimal pesoGramos) { this.pesoGramos = pesoGramos; }
    public Double getCosto() { return costo; }
    public void setCosto(Double costo) { this.costo = costo; }
    public Double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(Double precioVenta) { this.precioVenta = precioVenta; }
    public Integer getExistencias() { return existencias; }
    public void setExistencias(Integer existencias) { this.existencias = existencias; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public Integer getCantidadDisponible() { return existencias; }
    public void setCantidadDisponible(Integer cantidadDisponible) { this.existencias = cantidadDisponible; }

    @Override
    public String toString() {
        return nombre;
    }
}
