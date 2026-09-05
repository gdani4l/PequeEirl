package com.peque.peque_backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.peque.peque_backend.models.TipoDocumento;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Integer> {

    Optional<TipoDocumento> findByAbreviatura(String abreviatura);
}