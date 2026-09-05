package com.peque.peque_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.peque.peque_backend.services.MisVentasService;

@RestController
@RequestMapping("/api/mis-ventas")
public class MisVentasController {

    @Autowired
    private MisVentasService misVentasService;

    private Integer usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) {
            return null;
        }
        try {
            return Integer.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        Integer idUsuario = usuarioActual();
        if (idUsuario == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(misVentasService.listarMisVentas(idUsuario));
    }

    @GetMapping("/resumen")
    public ResponseEntity<?> resumen() {
        Integer idUsuario = usuarioActual();
        if (idUsuario == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(misVentasService.resumen(idUsuario));
    }

    @GetMapping("/comprobante/{idVenta}")
    public ResponseEntity<byte[]> comprobante(@PathVariable Integer idVenta) {
        Integer idUsuario = usuarioActual();
        if (idUsuario == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            byte[] pdf = misVentasService.comprobanteDelPropietario(idVenta, idUsuario);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"boleta_" + idVenta + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        }
    }
}
