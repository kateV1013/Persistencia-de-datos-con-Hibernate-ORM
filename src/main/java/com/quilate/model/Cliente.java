package com.quilate.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(length = 60)
    private String apellido;

    @Column(length = 20)
    private String telefono;

    @Column(unique = true, length = 100)
    private String correo;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro = LocalDate.now();

    @OneToMany(mappedBy = "cliente", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Apartado> apartados = new ArrayList<>();

    @OneToMany(mappedBy = "cliente")
    private List<PedidoPersonalizado> pedidos = new ArrayList<>();

    @OneToMany(mappedBy = "cliente")
    private List<Venta> ventas = new ArrayList<>();

    public Cliente() {
    }

    public Cliente(String nombre, String telefono, String correo) {
        this(nombre, null, telefono, correo);
    }

    public Cliente(String nombre, String apellido, String telefono, String correo) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.correo = correo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public List<Apartado> getApartados() { return apartados; }
    public void setApartados(List<Apartado> apartados) { this.apartados = apartados; }
    public List<PedidoPersonalizado> getPedidos() { return pedidos; }
    public List<Venta> getVentas() { return ventas; }

    public String getNombreCompleto() {
        return apellido == null || apellido.isBlank() ? nombre : nombre + " " + apellido;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Cliente otro && id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Cliente.class.hashCode();
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}
