package com.peque.peque_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.peque.peque_backend.models.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
    
    @Query("SELECT p FROM Producto p WHERE p.categoria.id_categoria = :idCategoria")
    List<Producto> findByCategoriaId(@Param("idCategoria") Integer idCategoria);
    
    @Query("SELECT p FROM Producto p WHERE p.presentacion.id_presentacion = :idPresentacion")
    List<Producto> findByPresentacionId(@Param("idPresentacion") Integer idPresentacion);
}