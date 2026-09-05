package com.peque.peque_backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.peque.peque_backend.dtos.CategoriaDTO;
import com.peque.peque_backend.dtos.PresentacionDTO;
import com.peque.peque_backend.dtos.ProductoResponseDTO;
import com.peque.peque_backend.models.Categoria;
import com.peque.peque_backend.models.PresentacionProducto;
import com.peque.peque_backend.models.Producto;
import com.peque.peque_backend.repositories.CategoriaRepository;
import com.peque.peque_backend.repositories.PresentacionProductoRepository;
import com.peque.peque_backend.repositories.ProductoRepository;

@Service
public class ProductoService {
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private PresentacionProductoRepository presentacionProductoRepository;
    
    public List<ProductoResponseDTO> getProductos(String nombre, Integer idCategoria, Integer idPresentacion) {
        List<Producto> productos;
        
        if (nombre != null && !nombre.isEmpty()) {
            productos = productoRepository.findByNombreContainingIgnoreCase(nombre);
        } else if (idCategoria != null) {
            productos = productoRepository.findByCategoriaId(idCategoria);
        } else if (idPresentacion != null) {
            productos = productoRepository.findByPresentacionId(idPresentacion);
        } else {
            productos = productoRepository.findAll();
        }
        
        return productos.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    private ProductoResponseDTO convertToDTO(Producto producto) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId_producto(producto.getId_producto());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio_unitario(producto.getPrecio_unitario());
        dto.setStock(producto.getStock());
        
        if (producto.getCategoria() != null) {
            dto.setCategoria(producto.getCategoria().getNombre());
            dto.setId_categoria(producto.getCategoria().getId_categoria());
        }
        
        if (producto.getOrigen() != null) {
            dto.setOrigen(producto.getOrigen().getNombre());
        }
        
        if (producto.getPresentacion() != null) {
            dto.setPresentacion(producto.getPresentacion().getNombre());
            dto.setId_presentacion(producto.getPresentacion().getId_presentacion());
        }
        
        if (producto.getFechaVencimiento() != null) {
            dto.setFecha_vencimiento(producto.getFechaVencimiento().getFecha_vencimiento());
        }
        
        return dto;
    }
    
    public List<CategoriaDTO> getCategorias() {
        return categoriaRepository.findAll().stream()
                .map(c -> new CategoriaDTO(c.getId_categoria(), c.getNombre()))
                .collect(Collectors.toList());
    }
    
    public List<PresentacionDTO> getPresentaciones() {
        return presentacionProductoRepository.findAll().stream()
                .map(p -> new PresentacionDTO(p.getId_presentacion(), p.getNombre()))
                .collect(Collectors.toList());
    }
}