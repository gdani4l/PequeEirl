package com.peque.peque_backend.repositories;

import com.peque.peque_backend.models.DetalleNotaCredito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetalleNotaCreditoRepository extends JpaRepository<DetalleNotaCredito, Integer> {

    @Query("SELECT d FROM DetalleNotaCredito d WHERE d.notaCredito.id_nota_credito = :idNotaCredito")
    List<DetalleNotaCredito> buscarPorNotaCredito(@Param("idNotaCredito") Integer idNotaCredito);
}
