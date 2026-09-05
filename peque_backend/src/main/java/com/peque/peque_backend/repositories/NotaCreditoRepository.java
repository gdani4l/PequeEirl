package com.peque.peque_backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.peque.peque_backend.models.NotaCredito;

@Repository
public interface NotaCreditoRepository extends JpaRepository<NotaCredito, Integer> {

    @Query("SELECT MAX(n.numero) FROM NotaCredito n WHERE n.serie = :serie")
    Optional<Integer> findMaxNumeroBySerie(@Param("serie") String serie);

    @Query("SELECT n FROM NotaCredito n WHERE n.ventaOriginal.id_venta = :ventaId")
    Optional<NotaCredito> findByVentaOriginalId(@Param("ventaId") Integer ventaId);
}