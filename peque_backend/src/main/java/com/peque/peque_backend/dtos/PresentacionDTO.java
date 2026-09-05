package com.peque.peque_backend.dtos;

public class PresentacionDTO {
    private Integer id_presentacion;
    private String nombre;

    public PresentacionDTO(Integer id_presentacion, String nombre) {
        this.id_presentacion = id_presentacion;
        this.nombre = nombre;
    }

    public Integer getId_presentacion() {
        return id_presentacion;
    }

    public void setId_presentacion(Integer id_presentacion) {
        this.id_presentacion = id_presentacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}