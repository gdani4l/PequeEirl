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
@Table(name = "telefonos")
public class Telefonos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_telefono;
    
    @Column(nullable = false, length = 20)
    private String numero;
    
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
    
    public Telefonos() {}
    
    public Telefonos(String numero, Usuario usuario) {
        this.numero = numero;
        this.usuario = usuario;
    }
    
    public Integer getId_telefono() {
        return id_telefono;
    }
    
    public void setId_telefono(Integer id_telefono) {
        this.id_telefono = id_telefono;
    }
    
    public String getNumero() {
        return numero;
    }
    
    public void setNumero(String numero) {
        this.numero = numero;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}