package com.peque.peque_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "origen_producto")
public class OrigenProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_origen;
    
    @Column(nullable = false, length = 50)
    private String nombre;
    
    public OrigenProducto() {}
    
    public OrigenProducto(String nombre) {
        this.nombre = nombre;
    }
    
    public Integer getId_origen() {
        return id_origen;
    }
    
    public void setId_origen(Integer id_origen) {
        this.id_origen = id_origen;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}