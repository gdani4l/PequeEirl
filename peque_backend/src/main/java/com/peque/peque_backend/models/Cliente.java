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
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_cliente;
    
    @ManyToOne
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;
    
    private String nombre;
    
    private String segundo_nombre;
    
    private String apellido_pat;
    
    private String apellido_mat;
    
    private String razon_social;
    
    private String domicilio_fiscal;
    
    private java.time.LocalDateTime fecha_registro;
    
    @Column(nullable = false, length = 20)
    private String numero_documento;
    
    public Cliente() {}
    
    public Cliente(TipoDocumento tipoDocumento, String numero_documento) {
        this.tipoDocumento = tipoDocumento;
        this.numero_documento = numero_documento;
        this.fecha_registro = java.time.LocalDateTime.now();
    }
    
    public Integer getId_cliente() {
        return id_cliente;
    }
    
    public void setId_cliente(Integer id_cliente) {
        this.id_cliente = id_cliente;
    }
    
    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }
    
    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getSegundo_nombre() {
        return segundo_nombre;
    }
    
    public void setSegundo_nombre(String segundo_nombre) {
        this.segundo_nombre = segundo_nombre;
    }
    
    public String getApellido_pat() {
        return apellido_pat;
    }
    
    public void setApellido_pat(String apellido_pat) {
        this.apellido_pat = apellido_pat;
    }
    
    public String getApellido_mat() {
        return apellido_mat;
    }
    
    public void setApellido_mat(String apellido_mat) {
        this.apellido_mat = apellido_mat;
    }
    
    public String getRazon_social() {
        return razon_social;
    }
    
    public void setRazon_social(String razon_social) {
        this.razon_social = razon_social;
    }
    
    public String getDomicilio_fiscal() {
        return domicilio_fiscal;
    }
    
    public void setDomicilio_fiscal(String domicilio_fiscal) {
        this.domicilio_fiscal = domicilio_fiscal;
    }
    
    public java.time.LocalDateTime getFecha_registro() {
        return fecha_registro;
    }
    
    public void setFecha_registro(java.time.LocalDateTime fecha_registro) {
        this.fecha_registro = fecha_registro;
    }
    
    public String getNumero_documento() {
        return numero_documento;
    }
    
    public void setNumero_documento(String numero_documento) {
        this.numero_documento = numero_documento;
    }
}