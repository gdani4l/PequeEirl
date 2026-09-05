package com.peque.peque_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.peque.peque_backend.models.PresentacionProducto;

@Repository
public interface PresentacionProductoRepository extends JpaRepository<PresentacionProducto, Integer> {
}