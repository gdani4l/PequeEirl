package com.peque.peque_backend.dtos;

public class AnularVentaRequestDTO {
    private Integer idUsuario;
    private String motivo;

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
