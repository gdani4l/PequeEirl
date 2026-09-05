package com.peque.peque_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "mfa_codigo")
public class MfaCodigo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_mfa;

    @Column(nullable = false)
    private Integer id_usuario;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private LocalDateTime expira;

    @Column(nullable = false)
    private Boolean usado;

    @Column(nullable = false)
    private Integer intentos;

    private LocalDateTime fecha_creacion;

    public Integer getId_mfa() {
        return id_mfa;
    }

    public void setId_mfa(Integer id_mfa) {
        this.id_mfa = id_mfa;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDateTime getExpira() {
        return expira;
    }

    public void setExpira(LocalDateTime expira) {
        this.expira = expira;
    }

    public Boolean getUsado() {
        return usado;
    }

    public void setUsado(Boolean usado) {
        this.usado = usado;
    }

    public Integer getIntentos() {
        return intentos;
    }

    public void setIntentos(Integer intentos) {
        this.intentos = intentos;
    }

    public LocalDateTime getFecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(LocalDateTime fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }
}
