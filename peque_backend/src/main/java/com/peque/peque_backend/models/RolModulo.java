package com.peque.peque_backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rol_modulo")
public class RolModulo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_rol_modulo;
    
    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;
    
    @ManyToOne
    @JoinColumn(name = "id_modulo", nullable = false)
    private Modulo modulo;
    
    public RolModulo() {}
    
    public RolModulo(Rol rol, Modulo modulo) {
        this.rol = rol;
        this.modulo = modulo;
    }
    
    public Integer getId_rol_modulo() {
        return id_rol_modulo;
    }
    
    public void setId_rol_modulo(Integer id_rol_modulo) {
        this.id_rol_modulo = id_rol_modulo;
    }
    
    public Rol getRol() {
        return rol;
    }
    
    public void setRol(Rol rol) {
        this.rol = rol;
    }
    
    public Modulo getModulo() {
        return modulo;
    }
    
    public void setModulo(Modulo modulo) {
        this.modulo = modulo;
    }
}