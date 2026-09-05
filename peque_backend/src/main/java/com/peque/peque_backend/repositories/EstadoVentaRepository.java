package com.peque.peque_backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.peque.peque_backend.models.EstadoVenta;

@Repository
public interface EstadoVentaRepository extends JpaRepository<EstadoVenta, Integer> {

    Optional<EstadoVenta> findByNombre(String nombre);
}