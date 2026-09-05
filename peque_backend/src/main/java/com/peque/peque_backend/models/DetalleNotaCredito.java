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
@Table(name = "detalle_nota_credito")
public class DetalleNotaCredito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_detalle_nota;
    
    @ManyToOne
    @JoinColumn(name = "id_nota_credito", nullable = false)
    private NotaCredito notaCredito;
    
    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio_unitario;
    
    public DetalleNotaCredito() {}
    
    public DetalleNotaCredito(NotaCredito notaCredito, Producto producto, BigDecimal cantidad, BigDecimal precio_unitario) {
        this.notaCredito = notaCredito;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precio_unitario = precio_unitario;
    }
    
    public Integer getId_detalle_nota() {
        return id_detalle_nota;
    }
    
    public void setId_detalle_nota(Integer id_detalle_nota) {
        this.id_detalle_nota = id_detalle_nota;
    }
    
    public NotaCredito getNotaCredito() {
        return notaCredito;
    }
    
    public void setNotaCredito(NotaCredito notaCredito) {
        this.notaCredito = notaCredito;
    }
    
    public Producto getProducto() {
        return producto;
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
    }
    
    public BigDecimal getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }
    
    public BigDecimal getPrecio_unitario() {
        return precio_unitario;
    }
    
    public void setPrecio_unitario(BigDecimal precio_unitario) {
        this.precio_unitario = precio_unitario;
    }
}