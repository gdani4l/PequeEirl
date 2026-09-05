package com.peque.peque_backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.peque.peque_backend.models.Telefonos;

@Repository
public interface TelefonosRepository extends JpaRepository<Telefonos, Integer> {
    
    @Query("SELECT t FROM Telefonos t WHERE t.usuario.id_usuario = :idUsuario")
    Optional<Telefonos> findByUsuarioId(@Param("idUsuario") Integer idUsuario);
}