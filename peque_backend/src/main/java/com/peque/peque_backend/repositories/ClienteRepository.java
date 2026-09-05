package com.peque.peque_backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.peque.peque_backend.models.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    
    @Query("SELECT c FROM Cliente c WHERE c.numero_documento = :numeroDocumento")
    Optional<Cliente> findByNumeroDocumento(@Param("numeroDocumento") String numeroDocumento);
}