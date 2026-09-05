package com.peque.peque_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "movimientos")
public class Movimientos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_movimiento;
    
    @ManyToOne
    @JoinColumn(name = "id_venta", nullable = false)
    private Venta venta;
    
    @ManyToOne
    @JoinColumn(name = "id_estado_anterior")
    private EstadoVenta estadoAnterior;
    
    @ManyToOne
    @JoinColumn(name = "id_estado_nuevo", nullable = false)
    private EstadoVenta estadoNuevo;
    
    private java.time.LocalDateTime fecha_movimiento;
    
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @Column(length = 200)
    private String observacion;
    
    public Movimientos() {}
    
    public Movimientos(Venta venta, EstadoVenta estadoNuevo, Usuario usuario) {
        this.venta = venta;
        this.estadoNuevo = estadoNuevo;
        this.usuario = usuario;
        this.fecha_movimiento = java.time.LocalDateTime.now();
    }
    
    public Integer getId_movimiento() {
        return id_movimiento;
    }
    
    public void setId_movimiento(Integer id_movimiento) {
        this.id_movimiento = id_movimiento;
    }
    
    public Venta getVenta() {
        return venta;
    }
    
    public void setVenta(Venta venta) {
        this.venta = venta;
    }
    
    public EstadoVenta getEstadoAnterior() {
        return estadoAnterior;
    }
    
    public void setEstadoAnterior(EstadoVenta estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }
    
    public EstadoVenta getEstadoNuevo() {
        return estadoNuevo;
    }
    
    public void setEstadoNuevo(EstadoVenta estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }
    
    public java.time.LocalDateTime getFecha_movimiento() {
        return fecha_movimiento;
    }
    
    public void setFecha_movimiento(java.time.LocalDateTime fecha_movimiento) {
        this.fecha_movimiento = fecha_movimiento;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public String getObservacion() {
        return observacion;
    }
    
    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}