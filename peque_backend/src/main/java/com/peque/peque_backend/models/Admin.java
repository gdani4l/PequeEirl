package com.peque.peque_backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin")
public class Admin {
    @Id
    private Integer id_admin;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "id_admin")
    private Usuario usuario;
    
    public Admin() {}
    
    public Admin(Usuario usuario) {
        this.usuario = usuario;
        this.id_admin = usuario.getId_usuario();
    }
    
    public Integer getId_admin() {
        return id_admin;
    }
    
    public void setId_admin(Integer id_admin) {
        this.id_admin = id_admin;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        this.id_admin = usuario.getId_usuario();
    }
}