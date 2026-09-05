package com.peque.peque_backend.dtos;

import java.math.BigDecimal;

public class NiubizSesionRequestDTO {

    private BigDecimal monto;
    private VentaRequestDTO venta;

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
