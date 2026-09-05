package com.peque.peque_backend.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_producto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio_unitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal stock;

    @Column(nullable = false, length = 250)
    private String descripcion;

    @Column(nullable = false, length = 50)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "id_origen")
    private OrigenProducto origen;

    @ManyToOne
    @JoinColumn(name = "id_presentacion")
    private PresentacionProducto presentacion;

    @ManyToOne
    @JoinColumn(name = "id_fecha_vencimiento")
    private FVencimiento fechaVencimiento;

    public Producto() {
    }

    public Producto(BigDecimal precio_unitario, BigDecimal stock, String descripcion, String nombre,
            Categoria categoria, OrigenProducto origen, PresentacionProducto presentacion,
            FVencimiento fechaVencimiento) {
        this.precio_unitario = precio_unitario;
        this.stock = stock;
        this.descripcion = descripcion;
        this.nombre = nombre;
        this.categoria = categoria;
        this.origen = origen;
        this.presentacion = presentacion;
        this.fechaVencimiento = fechaVencimiento;
    }

    public Integer getId_producto() {
        return id_producto;
    }

    public void setId_producto(Integer id_producto) {
        this.id_producto = id_producto;
    }

    public BigDecimal getPrecio_unitario() {
        return precio_unitario;
    }

    public void setPrecio_unitario(BigDecimal precio_unitario) {
        this.precio_unitario = precio_unitario;
    }

    public BigDecimal getStock() {
        return stock;
    }

    public void setStock(BigDecimal stock) {
        this.stock = stock;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public OrigenProducto getOrigen() {
        return origen;
    }

    public void setOrigen(OrigenProducto origen) {
        this.origen = origen;
    }

    public PresentacionProducto getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(PresentacionProducto presentacion) {
        this.presentacion = presentacion;
    }

    public FVencimiento getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(FVencimiento fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
}