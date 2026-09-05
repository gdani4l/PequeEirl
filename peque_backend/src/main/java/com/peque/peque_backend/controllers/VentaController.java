package com.peque.peque_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.peque.peque_backend.dtos.VentaRequestDTO;
import com.peque.peque_backend.dtos.VentaResponseDTO;
import com.peque.peque_backend.services.VentaService;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @PostMapping
    public ResponseEntity<VentaResponseDTO> registrarVenta(@RequestBody VentaRequestDTO request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) {
            try {
                request.setIdUsuario(Integer.valueOf(auth.getName()));
            } catch (NumberFormatException ignored) {
            }
        }
        VentaResponseDTO response = ventaService.registrarVenta(request);
        return ResponseEntity.ok(response);
    }
}
