package com.peque.peque_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categoria")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_categoria;
    
    @Column(nullable = false, length = 50)
    private String nombre;
    
    public Categoria() {}
    
    public Categoria(String nombre) {
        this.nombre = nombre;
    }
    
    public Integer getId_categoria() {
        return id_categoria;
    }
    
    public void setId_categoria(Integer id_categoria) {
        this.id_categoria = id_categoria;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}