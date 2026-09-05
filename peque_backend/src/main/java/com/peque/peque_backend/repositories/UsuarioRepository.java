package com.peque.peque_backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.peque.peque_backend.models.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    Optional<Usuario> findByCorreo(String correo);
    
    boolean existsByCorreo(String correo);
    
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Telefonos t WHERE t.numero = :telefono AND t.usuario.id_usuario != :idUsuario")
    boolean existsTelefonoDuplicado(@Param("telefono") String telefono, @Param("idUsuario") Integer idUsuario);
}