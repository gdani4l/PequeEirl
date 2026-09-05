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
@Table(name = "detalle_venta")
public class DetalleVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_detalle;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio_fijo;
    
    @ManyToOne
    @JoinColumn(name = "id_venta")
    private Venta venta;
    
    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;
    
    public DetalleVenta() {}
    
    public DetalleVenta(BigDecimal cantidad, BigDecimal precio_fijo, Venta venta, Producto producto) {
        this.cantidad = cantidad;
        this.precio_fijo = precio_fijo;
        this.venta = venta;
        this.producto = producto;
    }
    
    public Integer getId_detalle() {
        return id_detalle;
    }
    
    public void setId_detalle(Integer id_detalle) {
        this.id_detalle = id_detalle;
    }
    
    public BigDecimal getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }
    
    public BigDecimal getPrecio_fijo() {
        return precio_fijo;
    }
    
    public void setPrecio_fijo(BigDecimal precio_fijo) {
        this.precio_fijo = precio_fijo;
    }
    
    public Venta getVenta() {
        return venta;
    }
    
    public void setVenta(Venta venta) {
        this.venta = venta;
    }
    
    public Producto getProducto() {
        return producto;
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
    }
}