package com.peque.peque_backend.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_verification_token")
public class EmailVerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_token;
    
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @Column(nullable = false, length = 255)
    private String token;
    
    @Column(nullable = false)
    private LocalDateTime expires_at;
    
    @Column(nullable = false)
    private Boolean is_used;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime created_at;
    
    public EmailVerificationToken() {
        this.created_at = LocalDateTime.now();
    }
    
    public EmailVerificationToken(Usuario usuario, String token, LocalDateTime expires_at) {
        this.usuario = usuario;
        this.token = token;
        this.expires_at = expires_at;
        this.is_used = false;
        this.created_at = LocalDateTime.now();
    }
    
    public Integer getId_token() {
        return id_token;
    }
    
    public void setId_token(Integer id_token) {
        this.id_token = id_token;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public LocalDateTime getExpires_at() {
        return expires_at;
    }
    
    public void setExpires_at(LocalDateTime expires_at) {
        this.expires_at = expires_at;
    }
    
    public Boolean getIs_used() {
        return is_used;
    }
    
    public void setIs_used(Boolean is_used) {
        this.is_used = is_used;
    }
    
    public LocalDateTime getCreated_at() {
        return created_at;
    }
    
    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}