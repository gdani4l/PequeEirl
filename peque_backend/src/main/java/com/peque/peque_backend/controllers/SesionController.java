package com.peque.peque_backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sesion")
public class SesionController {

    @GetMapping("/verificar")
    public ResponseEntity<Void> verificar() {
        return ResponseEntity.ok().build();
    }
}
