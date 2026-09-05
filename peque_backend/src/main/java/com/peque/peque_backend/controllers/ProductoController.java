package com.peque.peque_backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.peque.peque_backend.dtos.ProductoResponseDTO;
import com.peque.peque_backend.dtos.CategoriaDTO;
import com.peque.peque_backend.dtos.PresentacionDTO;
import com.peque.peque_backend.services.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> getProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(required = false) Integer idPresentacion) {

        List<ProductoResponseDTO> productos = productoService.getProductos(nombre, idCategoria, idPresentacion);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaDTO>> getCategorias() {
        return ResponseEntity.ok(productoService.getCategorias());
    }

    @GetMapping("/presentaciones")
    public ResponseEntity<List<PresentacionDTO>> getPresentaciones() {
        return ResponseEntity.ok(productoService.getPresentaciones());
    }
}