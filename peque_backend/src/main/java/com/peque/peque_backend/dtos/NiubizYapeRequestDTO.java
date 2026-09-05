package com.peque.peque_backend.dtos;

import java.math.BigDecimal;

public class NiubizYapeRequestDTO {

    private String celular;
    private String otp;
    private BigDecimal monto;
    private VentaRequestDTO venta;

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public VentaRequestDTO getVenta() {
        return venta;
    }

    public void setVenta(VentaRequestDTO venta) {
        this.venta = venta;
    }
}
