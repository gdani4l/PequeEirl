package com.peque.peque_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago_niubiz")
public class PagoNiubiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_pago_niubiz;

    @Column(unique = true, nullable = false)
    private Long purchase_number;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    private String estado;

    private String transaction_token;

    private String action_code;

    private String descripcion;

    private String tarjeta;

    private String marca;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String venta_json;

    private Integer id_venta;

    private LocalDateTime fecha_creacion;

    private LocalDateTime fecha_respuesta;

    public Integer getId_pago_niubiz() {
        return id_pago_niubiz;
    }

    public void setId_pago_niubiz(Integer id_pago_niubiz) {
        this.id_pago_niubiz = id_pago_niubiz;
    }

    public Long getPurchase_number() {
        return purchase_number;
    }

    public void setPurchase_number(Long purchase_number) {
        this.purchase_number = purchase_number;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTransaction_token() {
        return transaction_token;
    }

    public void setTransaction_token(String transaction_token) {
        this.transaction_token = transaction_token;
    }

    public String getAction_code() {
        return action_code;
    }

    public void setAction_code(String action_code) {
        this.action_code = action_code;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(String tarjeta) {
        this.tarjeta = tarjeta;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getVenta_json() {
        return venta_json;
    }

    public void setVenta_json(String venta_json) {
        this.venta_json = venta_json;
    }

    public Integer getId_venta() {
        return id_venta;
    }

    public void setId_venta(Integer id_venta) {
        this.id_venta = id_venta;
    }

    public LocalDateTime getFecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(LocalDateTime fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    public LocalDateTime getFecha_respuesta() {
        return fecha_respuesta;
    }

    public void setFecha_respuesta(LocalDateTime fecha_respuesta) {
        this.fecha_respuesta = fecha_respuesta;
    }
}
