package com.peque.peque_backend.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimientoResponseDTO {
    private Integer idMovimiento;
    private Integer idVenta;
    private String serieVenta;
    private Integer numeroVenta;
    private BigDecimal totalVenta;
    private String tipoComprobante;
    private String estadoAnterior;
    private String estadoNuevo;
    private LocalDateTime fechaMovimiento;
    private String usuarioResponsable;
    private String observacion;
    private boolean tieneNotaCredito;
    private Integer idNotaCredito;

    public MovimientoResponseDTO() {}

    public Integer getIdMovimiento() { return idMovimiento; }
    public void setIdMovimiento(Integer idMovimiento) { this.idMovimiento = idMovimiento; }

    public Integer getIdVenta() { return idVenta; }
    public void setIdVenta(Integer idVenta) { this.idVenta = idVenta; }

    public String getSerieVenta() { return serieVenta; }
    public void setSerieVenta(String serieVenta) { this.serieVenta = serieVenta; }

    public Integer getNumeroVenta() { return numeroVenta; }
    public void setNumeroVenta(Integer numeroVenta) { this.numeroVenta = numeroVenta; }

    public BigDecimal getTotalVenta() { return totalVenta; }
    public void setTotalVenta(BigDecimal totalVenta) { this.totalVenta = totalVenta; }

    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }

    public String getUsuarioResponsable() { return usuarioResponsable; }
    public void setUsuarioResponsable(String usuarioResponsable) { this.usuarioResponsable = usuarioResponsable; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public boolean isTieneNotaCredito() { return tieneNotaCredito; }
    public void setTieneNotaCredito(boolean tieneNotaCredito) { this.tieneNotaCredito = tieneNotaCredito; }

    public Integer getIdNotaCredito() { return idNotaCredito; }
    public void setIdNotaCredito(Integer idNotaCredito) { this.idNotaCredito = idNotaCredito; }
}
