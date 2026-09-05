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
@Table(name = "direcciones")
public class Direcciones {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_direccion;
    
    @Column(nullable = false, length = 50)
    private String departamento;
    
    @Column(nullable = false, length = 50)
    private String provincia;
    
    @Column(nullable = false, length = 50)
    private String distrito;
    
    @Column(nullable = false, length = 100)
    private String calle;
    
    @Column(length = 150)
    private String referencia;
    
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
    
    public Direcciones() {}
    
    public Direcciones(String departamento, String provincia, String distrito, String calle, String referencia, Usuario usuario) {
        this.departamento = departamento;
        this.provincia = provincia;
        this.distrito = distrito;
        this.calle = calle;
        this.referencia = referencia;
        this.usuario = usuario;
    }
    
    public Integer getId_direccion() {
        return id_direccion;
    }
    
    public void setId_direccion(Integer id_direccion) {
        this.id_direccion = id_direccion;
    }
    
    public String getDepartamento() {
        return departamento;
    }
    
    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }
    
    public String getProvincia() {
        return provincia;
    }
    
    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }
    
    public String getDistrito() {
        return distrito;
    }
    
    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }
    
    public String getCalle() {
        return calle;
    }
    
    public void setCalle(String calle) {
        this.calle = calle;
    }
    
    public String getReferencia() {
        return referencia;
    }
    
    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}