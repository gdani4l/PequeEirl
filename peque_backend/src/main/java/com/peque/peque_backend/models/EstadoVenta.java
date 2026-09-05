package com.peque.peque_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "estado_venta")
public class EstadoVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_estado_venta;
    
    @Column(nullable = false, length = 30, unique = true)
    private String nombre;
    
    public EstadoVenta() {}
    
    public EstadoVenta(String nombre) {
        this.nombre = nombre;
    }
    
    public Integer getId_estado_venta() {
        return id_estado_venta;
    }
    
    public void setId_estado_venta(Integer id_estado_venta) {
        this.id_estado_venta = id_estado_venta;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}