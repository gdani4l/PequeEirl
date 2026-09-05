package com.peque.peque_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.peque.peque_backend.models.DetalleVenta;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {

    @Query("SELECT u.nombre, COUNT(dv.id_detalle), SUM(dv.cantidad * dv.precio_fijo) " +
           "FROM DetalleVenta dv " +
           "JOIN dv.venta v " +
           "JOIN v.usuario u " +
           "WHERE v.estadoVenta.id_estado_venta = 6 " +
           "GROUP BY u.id_usuario, u.nombre " +
           "ORDER BY SUM(dv.cantidad * dv.precio_fijo) DESC")
    List<Object[]> findTopVendedores();

    @Query("SELECT dv FROM DetalleVenta dv WHERE dv.venta.id_venta = :idVenta")
    List<DetalleVenta> findByVentaId(Integer idVenta);
}