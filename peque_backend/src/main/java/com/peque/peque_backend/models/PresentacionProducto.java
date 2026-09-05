package com.peque.peque_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "presentacion_producto")
public class PresentacionProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_presentacion;
    
    @Column(nullable = false, length = 50)
    private String nombre;
    
    public PresentacionProducto() {}
    
    public PresentacionProducto(String nombre) {
        this.nombre = nombre;
    }
    
    public Integer getId_presentacion() {
        return id_presentacion;
    }
    
    public void setId_presentacion(Integer id_presentacion) {
        this.id_presentacion = id_presentacion;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}