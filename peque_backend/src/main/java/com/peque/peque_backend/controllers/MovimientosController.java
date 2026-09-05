package com.peque.peque_backend.controllers;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.peque.peque_backend.dtos.AnularVentaRequestDTO;
import com.peque.peque_backend.dtos.MensajeResponseDTO;
import com.peque.peque_backend.services.MovimientosService;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientosController {

    @Autowired
    private MovimientosService movimientosService;

    private void aplicarUsuarioDelToken(AnularVentaRequestDTO request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (request != null && auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) {
            try {
                request.setIdUsuario(Integer.valueOf(auth.getName()));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @GetMapping
    public ResponseEntity<?> listarMovimientos() {
        try {
            List<Map<String, Object>> movimientos = movimientosService.listarMovimientos();
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/anular/{idVenta}")
    public ResponseEntity<?> anularVenta(
            @PathVariable Integer idVenta,
            @RequestBody AnularVentaRequestDTO request) {
        try {
            aplicarUsuarioDelToken(request);
            Map<String, Object> result = movimientosService.anularVenta(idVenta, request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MensajeResponseDTO(e.getMessage()));
        }
    }

    @PostMapping("/restaurar/{idVenta}")
    public ResponseEntity<?> restaurarVenta(
            @PathVariable Integer idVenta,
            @RequestBody AnularVentaRequestDTO request) {
        try {
            aplicarUsuarioDelToken(request);
            Map<String, Object> result = movimientosService.restaurarVenta(idVenta, request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MensajeResponseDTO(e.getMessage()));
        }
    }

    @GetMapping("/comprobante/{idVenta}")
    public ResponseEntity<byte[]> descargarComprobante(@PathVariable Integer idVenta) {
        byte[] pdf = movimientosService.generarComprobantePDF(idVenta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprobante_" + idVenta + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/nota-credito/{idNotaCredito}")
    public ResponseEntity<byte[]> descargarNotaCredito(@PathVariable Integer idNotaCredito) {
        byte[] pdf = movimientosService.generarNotaCreditoPDF(idNotaCredito);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nota_credito_" + idNotaCredito + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}