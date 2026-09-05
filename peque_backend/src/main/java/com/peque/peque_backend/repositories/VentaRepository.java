package com.peque.peque_backend.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.peque.peque_backend.models.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    @Query("SELECT MAX(v.numero) FROM Venta v WHERE v.serie = :serie")
    Optional<Integer> findMaxNumeroBySerie(@Param("serie") String serie);

    @Query("SELECT v FROM Venta v WHERE v.fecha_venta BETWEEN :inicio AND :fin")
    List<Venta> findVentasByFechaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT v FROM Venta v WHERE v.usuario.id_usuario = :idUsuario ORDER BY v.fecha_venta DESC")
    List<Venta> findByUsuarioId(@Param("idUsuario") Integer idUsuario);
}