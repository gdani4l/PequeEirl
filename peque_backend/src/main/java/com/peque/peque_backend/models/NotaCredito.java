package com.peque.peque_backend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "nota_credito")
public class NotaCredito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_nota_credito;
    
    @Column(nullable = false, length = 4)
    private String serie;
    
    @Column(nullable = false)
    private Integer numero;
    
    @Column(nullable = false)
    private LocalDateTime fecha_emision;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNota tipo;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;
    
    @Column(nullable = false, length = 200)
    private String motivo;
    
    @ManyToOne
    @JoinColumn(name = "id_venta_original", nullable = false)
    private Venta ventaOriginal;
    
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;
    
    public enum TipoNota {
        TOTAL, PARCIAL
    }
    
    public NotaCredito() {}
    
    public NotaCredito(String serie, Integer numero, TipoNota tipo, BigDecimal monto, String motivo, Venta ventaOriginal, Usuario usuario) {
        this.serie = serie;
        this.numero = numero;
        this.tipo = tipo;
        this.monto = monto;
        this.motivo = motivo;
        this.ventaOriginal = ventaOriginal;
        this.usuario = usuario;
        this.fecha_emision = LocalDateTime.now();
    }
    
    public Integer getId_nota_credito() {
        return id_nota_credito;
    }
    
    public void setId_nota_credito(Integer id_nota_credito) {
        this.id_nota_credito = id_nota_credito;
    }
    
    public String getSerie() {
        return serie;
    }
    
    public void setSerie(String serie) {
        this.serie = serie;
    }
    
    public Integer getNumero() {
        return numero;
    }
    
    public void setNumero(Integer numero) {
        this.numero = numero;
    }
    
    public LocalDateTime getFecha_emision() {
        return fecha_emision;
    }
    
    public void setFecha_emision(LocalDateTime fecha_emision) {
        this.fecha_emision = fecha_emision;
    }
    
    public TipoNota getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoNota tipo) {
        this.tipo = tipo;
    }
    
    public BigDecimal getMonto() {
        return monto;
    }
    
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
    public Venta getVentaOriginal() {
        return ventaOriginal;
    }
    
    public void setVentaOriginal(Venta ventaOriginal) {
        this.ventaOriginal = ventaOriginal;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
}