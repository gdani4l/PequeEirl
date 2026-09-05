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
@Table(name = "venta")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_venta;
    
    @Column(nullable = false, length = 4)
    private String serie;
    
    @Column(nullable = false)
    private Integer numero;
    
    private LocalDateTime fecha_anulacion;
    
    private String motivo_anulacion;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total_pago;
    
    @Column(nullable = false, length = 3)
    private String moneda;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CondicionPago condicion_pago;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPago metodo_pago;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor_venta;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal igv;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tasa_igv;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal isc;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal otros_tributos;
    
    @Column(nullable = false)
    private LocalDateTime fecha_venta;
    
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoComprobante tipo_comprobante;
    
    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;
    
    @ManyToOne
    @JoinColumn(name = "id_estado_venta", nullable = false)
    private EstadoVenta estadoVenta;
    
    public enum CondicionPago {
        CONTADO, CREDITO
    }
    
    public enum MetodoPago {
        EFECTIVO, YAPE, TARJETA
    }
    
    public enum TipoComprobante {
        BOLETA, FACTURA
    }
    
    public Integer getId_venta() {
        return id_venta;
    }
    
    public void setId_venta(Integer id_venta) {
        this.id_venta = id_venta;
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
    
    public LocalDateTime getFecha_anulacion() {
        return fecha_anulacion;
    }
    
    public void setFecha_anulacion(LocalDateTime fecha_anulacion) {
        this.fecha_anulacion = fecha_anulacion;
    }
    
    public String getMotivo_anulacion() {
        return motivo_anulacion;
    }
    
    public void setMotivo_anulacion(String motivo_anulacion) {
        this.motivo_anulacion = motivo_anulacion;
    }
    
    public BigDecimal getTotal_pago() {
        return total_pago;
    }
    
    public void setTotal_pago(BigDecimal total_pago) {
        this.total_pago = total_pago;
    }
    
    public String getMoneda() {
        return moneda;
    }
    
    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }
    
    public CondicionPago getCondicion_pago() {
        return condicion_pago;
    }
    
    public void setCondicion_pago(CondicionPago condicion_pago) {
        this.condicion_pago = condicion_pago;
    }
    
    public MetodoPago getMetodo_pago() {
        return metodo_pago;
    }
    
    public void setMetodo_pago(MetodoPago metodo_pago) {
        this.metodo_pago = metodo_pago;
    }
    
    public BigDecimal getValor_venta() {
        return valor_venta;
    }
    
    public void setValor_venta(BigDecimal valor_venta) {
        this.valor_venta = valor_venta;
    }
    
    public BigDecimal getIgv() {
        return igv;
    }
    
    public void setIgv(BigDecimal igv) {
        this.igv = igv;
    }
    
    public BigDecimal getTasa_igv() {
        return tasa_igv;
    }
    
    public void setTasa_igv(BigDecimal tasa_igv) {
        this.tasa_igv = tasa_igv;
    }
    
    public BigDecimal getIsc() {
        return isc;
    }
    
    public void setIsc(BigDecimal isc) {
        this.isc = isc;
    }
    
    public BigDecimal getOtros_tributos() {
        return otros_tributos;
    }
    
    public void setOtros_tributos(BigDecimal otros_tributos) {
        this.otros_tributos = otros_tributos;
    }
    
    public LocalDateTime getFecha_venta() {
        return fecha_venta;
    }
    
    public void setFecha_venta(LocalDateTime fecha_venta) {
        this.fecha_venta = fecha_venta;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public TipoComprobante getTipo_comprobante() {
        return tipo_comprobante;
    }
    
    public void setTipo_comprobante(TipoComprobante tipo_comprobante) {
        this.tipo_comprobante = tipo_comprobante;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    public EstadoVenta getEstadoVenta() {
        return estadoVenta;
    }
    
    public void setEstadoVenta(EstadoVenta estadoVenta) {
        this.estadoVenta = estadoVenta;
    }
}