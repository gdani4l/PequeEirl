package com.peque.peque_backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.peque.peque_backend.models.Direcciones;

@Repository
public interface DireccionesRepository extends JpaRepository<Direcciones, Integer> {
    
    @Query("SELECT d FROM Direcciones d WHERE d.usuario.id_usuario = :idUsuario")
    Optional<Direcciones> findByUsuarioId(@Param("idUsuario") Integer idUsuario);
}