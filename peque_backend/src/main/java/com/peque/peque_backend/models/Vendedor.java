package com.peque.peque_backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vendedor")
public class Vendedor {
    @Id
    private Integer id_vendedor;
    
    public Vendedor() {}
    
    public Vendedor(Integer id_vendedor) {
        this.id_vendedor = id_vendedor;
    }
    
    public Integer getId_vendedor() {
        return id_vendedor;
    }
    
    public void setId_vendedor(Integer id_vendedor) {
        this.id_vendedor = id_vendedor;
    }
}