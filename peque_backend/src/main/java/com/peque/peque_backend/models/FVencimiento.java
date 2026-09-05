package com.peque.peque_backend.models;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "f_vencimiento")
public class FVencimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_fecha_vencimiento;
    
    private LocalDate fecha_vencimiento;
    
    public FVencimiento() {}
    
    public FVencimiento(LocalDate fecha_vencimiento) {
        this.fecha_vencimiento = fecha_vencimiento;
    }
    
    public Integer getId_fecha_vencimiento() {
        return id_fecha_vencimiento;
    }
    
    public void setId_fecha_vencimiento(Integer id_fecha_vencimiento) {
        this.id_fecha_vencimiento = id_fecha_vencimiento;
    }
    
    public LocalDate getFecha_vencimiento() {
        return fecha_vencimiento;
    }
    
    public void setFecha_vencimiento(LocalDate fecha_vencimiento) {
        this.fecha_vencimiento = fecha_vencimiento;
    }
}