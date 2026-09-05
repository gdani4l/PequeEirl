package com.peque.peque_backend.repositories;

import com.peque.peque_backend.models.MfaCodigo;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MfaCodigoRepository extends JpaRepository<MfaCodigo, Integer> {

    @Query("SELECT m FROM MfaCodigo m WHERE m.id_usuario = :idUsuario AND m.usado = false ORDER BY m.id_mfa DESC LIMIT 1")
    Optional<MfaCodigo> buscarVigentePorUsuario(@Param("idUsuario") Integer idUsuario);

    @Query("SELECT m FROM MfaCodigo m WHERE m.id_usuario = :idUsuario AND m.usado = true AND m.intentos < 3 AND m.fecha_creacion > :desde ORDER BY m.id_mfa DESC LIMIT 1")
    Optional<MfaCodigo> buscarVerificadoReciente(@Param("idUsuario") Integer idUsuario, @Param("desde") LocalDateTime desde);
}
