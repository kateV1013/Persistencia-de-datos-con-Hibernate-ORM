package com.quilate.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "metodo_pago", nullable = false, length = 20)
    private String metodoPago = "EFECTIVO";

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO.setScale(2);

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVenta estado = EstadoVenta.PREPARACION;

    @Column(name = "inventario_comprometido", nullable = false)
    private boolean inventarioComprometido;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartado_origen_id", unique = true)
    private Apartado apartadoOrigen;

    @Version
    private long version;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<DetalleVenta> detalles = new ArrayList<>();

    public Venta() {
    }

    public Venta(Cliente cliente) {
        this.cliente = cliente;
    }

    /** Constructor conservado para los reportes y datos existentes. */
    public Venta(Cliente cliente, LocalDateTime fechaHora, String metodoPago, EstadoVenta estado) {
        this.cliente = cliente;
        this.fechaHora = fechaHora;
        this.metodoPago = metodoPago;
        this.estado = estado;
    }

    public void agregarDetalle(DetalleVenta detalle) {
        detalle.setVenta(this);
        detalles.add(detalle);
    }

    public void limpiarDetalles() {
        detalles.forEach(detalle -> detalle.setVenta(null));
        detalles.clear();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public EstadoVenta getEstado() { return estado; }
    public void setEstado(EstadoVenta estado) { this.estado = estado; }
    public boolean isInventarioComprometido() { return inventarioComprometido; }
    public void setInventarioComprometido(boolean inventarioComprometido) {
        this.inventarioComprometido = inventarioComprometido;
    }
    public Apartado getApartadoOrigen() { return apartadoOrigen; }
    public void setApartadoOrigen(Apartado apartadoOrigen) { this.apartadoOrigen = apartadoOrigen; }
    public Long getApartadoOrigenId() {
        return apartadoOrigen == null ? null : apartadoOrigen.getId();
    }
    public List<DetalleVenta> getDetalles() { return detalles; }
}
