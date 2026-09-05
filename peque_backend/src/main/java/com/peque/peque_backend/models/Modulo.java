package com.peque.peque_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "modulo")
public class Modulo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_modulo;
    
    @Column(nullable = false, length = 45, unique = true)
    private String nombre;
    
    @Column(length = 100)
    private String ruta;
    
    public Modulo() {}
    
    public Modulo(String nombre, String ruta) {
        this.nombre = nombre;
        this.ruta = ruta;
    }
    
    public Integer getId_modulo() {
        return id_modulo;
    }
    
    public void setId_modulo(Integer id_modulo) {
        this.id_modulo = id_modulo;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getRuta() {
        return ruta;
    }
    
    public void setRuta(String ruta) {
        this.ruta = ruta;
    }
}