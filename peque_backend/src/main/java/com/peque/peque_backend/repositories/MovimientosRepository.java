package com.peque.peque_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.peque.peque_backend.models.Movimientos;

@Repository
public interface MovimientosRepository extends JpaRepository<Movimientos, Integer> {

    @Query("SELECT m FROM Movimientos m ORDER BY m.fecha_movimiento DESC")
    List<Movimientos> findAllOrderByFechaDesc();

    @Query("SELECT m FROM Movimientos m WHERE m.venta.id_venta = :idVenta ORDER BY m.fecha_movimiento DESC")
    List<Movimientos> findByVentaId(@Param("idVenta") Integer idVenta);
}