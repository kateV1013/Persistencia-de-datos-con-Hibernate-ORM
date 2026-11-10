package com.quilate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Joya identificada por codigo de barras (tabla pieza de quilate_bd_inicial.sql).
 * Version base: el integrante responsable de Piezas agrega relaciones,
 * busqueda por codigo de barras y validaciones.
 */
@Entity
@Table(name = "pieza")
public class Pieza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pieza")
    private Integer id;

    @Column(name = "codigo_barras", nullable = false, unique = true, length = 30)
    private String codigoBarras;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "material", nullable = false, length = 50)
    private String material;

    @Column(name = "quilates")
    private Short quilates;

    @Column(name = "peso_gramos", precision = 6, scale = 2)
    private BigDecimal pesoGramos;

    @Column(name = "costo", nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;

    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    /** ANILLO, COLLAR, ARETES, PULSERA u OTRO (CHECK en la base). */
    @Column(name = "categoria", nullable = false, length = 20)
    private String categoria;

    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible = 0;

    public Pieza() {
    }

    public Pieza(String codigoBarras, String nombre, String material, BigDecimal costo,
                 BigDecimal precioVenta, String categoria, int cantidadDisponible) {
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.material = material;
        this.costo = costo;
        this.precioVenta = precioVenta;
        this.categoria = categoria;
        this.cantidadDisponible = cantidadDisponible;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public Short getQuilates() {
        return quilates;
    }

    public void setQuilates(Short quilates) {
        this.quilates = quilates;
    }

    public BigDecimal getPesoGramos() {
        return pesoGramos;
    }

    public void setPesoGramos(BigDecimal pesoGramos) {
        this.pesoGramos = pesoGramos;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(Integer cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
