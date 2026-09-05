package com.peque.peque_backend.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VentaResponseDTO {
    private Integer idVenta;
    private String serie;
    private Integer numero;
    private BigDecimal total;
    private LocalDateTime fechaVenta;
    private String mensaje;
    
    public VentaResponseDTO(Integer idVenta, String serie, Integer numero, BigDecimal total, LocalDateTime fechaVenta, String mensaje) {
        this.idVenta = idVenta;
        this.serie = serie;
        this.numero = numero;
        this.total = total;
        this.fechaVenta = fechaVenta;
        this.mensaje = mensaje;
    }
    
    public Integer getIdVenta() {
        return idVenta;
    }
    
    public void setIdVenta(Integer idVenta) {
        this.idVenta = idVenta;
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
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }
    
    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}