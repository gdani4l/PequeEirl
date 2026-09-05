package com.peque.peque_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.peque.peque_backend.models.Vendedor;

public interface VendedorRepository extends JpaRepository<Vendedor, Integer> {
}