package com.peque.peque_backend.repositories;

import com.peque.peque_backend.models.PagoNiubiz;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PagoNiubizRepository extends JpaRepository<PagoNiubiz, Integer> {

    @Query("SELECT p FROM PagoNiubiz p WHERE p.purchase_number = :purchaseNumber")
    Optional<PagoNiubiz> buscarPorPurchaseNumber(@Param("purchaseNumber") Long purchaseNumber);
}
