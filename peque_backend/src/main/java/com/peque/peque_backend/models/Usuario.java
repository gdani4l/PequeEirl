package com.peque.peque_backend.models;

import java.time.LocalDate;
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
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_usuario;

    @ManyToOne
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 50)
    private String segundo_nombre;

    @Column(nullable = false, length = 50)
    private String apellido_pat;

    @Column(nullable = false, length = 50)
    private String apellido_mat;

    @Column(nullable = false, length = 50, unique = true)
    private String correo;

    @Column(nullable = false, length = 250)
    private String password;

    @Column(nullable = false)
    private LocalDate fecha_registro;

    private Boolean activo;

    @Column(nullable = false)
    private Boolean email_verified;

    private LocalDateTime email_verified_at;

    @Column(nullable = false, length = 20)
    private String numero_documento;
    @Column(length = 10)
    private String codigo_postal;

    public Usuario() {
    }

    public Usuario(TipoDocumento tipoDocumento, Rol rol, String nombre, String apellido_pat, String apellido_mat,
            String correo, String password, LocalDate fecha_registro, String numero_documento) {
        this.tipoDocumento = tipoDocumento;
        this.rol = rol;
        this.nombre = nombre;
        this.apellido_pat = apellido_pat;
        this.apellido_mat = apellido_mat;
        this.correo = correo;
        this.password = password;
        this.fecha_registro = fecha_registro;
        this.numero_documento = numero_documento;
        this.activo = true;
        this.email_verified = false;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getFecha_registro() {
        return fecha_registro;
    }

    public void setFecha_registro(LocalDate fecha_registro) {
        this.fecha_registro = fecha_registro;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getEmail_verified() {
        return email_verified;
    }

    public void setEmail_verified(Boolean email_verified) {
        this.email_verified = email_verified;
    }

    public LocalDateTime getEmail_verified_at() {
        return email_verified_at;
    }

    public void setEmail_verified_at(LocalDateTime email_verified_at) {
        this.email_verified_at = email_verified_at;
    }

    public String getNumero_documento() {
        return numero_documento;
    }

    public void setNumero_documento(String numero_documento) {
        this.numero_documento = numero_documento;
    }

    public String getCodigo_postal() {
        return codigo_postal;
    }

    public void setCodigo_postal(String codigo_postal) {
        this.codigo_postal = codigo_postal;
    }
}