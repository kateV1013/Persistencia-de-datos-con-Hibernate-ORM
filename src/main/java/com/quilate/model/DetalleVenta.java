package com.quilate.model;

import com.quilate.service.CalculadoraVenta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "detalle_venta")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_venta", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pieza", nullable = false)
    private Pieza pieza;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "costo_unitario_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitarioVenta;

    /** Descuento monetario aplicado a cada unidad. */
    @Column(name = "descuento_aplicado", nullable = false, precision = 12, scale = 2)
    private BigDecimal descuentoAplicado = BigDecimal.ZERO.setScale(2);

    public DetalleVenta() {
    }

    /** Constructor para ventas: conserva precio, costo y descuento monetario por unidad. */
    public DetalleVenta(Pieza pieza, int cantidad, BigDecimal precioUnitario,
                        BigDecimal costoUnitarioVenta, BigDecimal descuentoAplicado) {
        this.pieza = pieza;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.costoUnitarioVenta = costoUnitarioVenta;
        this.descuentoAplicado = descuentoAplicado;
    }

    /** Constructor compatible con datos de reporte cuyo descuento se expresa como porcentaje. */
    public DetalleVenta(Pieza pieza, int cantidad, BigDecimal precioUnitario,
                        BigDecimal descuentoPct) {
        this(
                pieza,
                cantidad,
                precioUnitario,
                BigDecimal.valueOf(pieza.getCosto()),
                precioUnitario.multiply(descuentoPct)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        );
    }

    @Transient
    public BigDecimal getSubtotal() {
        return CalculadoraVenta.calcularSubtotal(
                precioUnitario, costoUnitarioVenta, descuentoAplicado, cantidad);
    }

    @Transient
    public BigDecimal getDescuentoPct() {
        if (precioUnitario == null || precioUnitario.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return descuentoAplicado.multiply(BigDecimal.valueOf(100))
                .divide(precioUnitario, 2, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }
    public Pieza getPieza() { return pieza; }
    public void setPieza(Pieza pieza) { this.pieza = pieza; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getCostoUnitarioVenta() { return costoUnitarioVenta; }
    public void setCostoUnitarioVenta(BigDecimal costoUnitarioVenta) {
        this.costoUnitarioVenta = costoUnitarioVenta;
    }
    public BigDecimal getDescuentoAplicado() { return descuentoAplicado; }
    public void setDescuentoAplicado(BigDecimal descuentoAplicado) {
        this.descuentoAplicado = descuentoAplicado;
    }
    public void setDescuentoPct(BigDecimal descuentoPct) {
        this.descuentoAplicado = precioUnitario.multiply(descuentoPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
