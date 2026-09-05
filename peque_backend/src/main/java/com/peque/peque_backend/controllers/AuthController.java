package com.peque.peque_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.peque.peque_backend.dtos.LoginRequestDTO;
import com.peque.peque_backend.dtos.LoginResponseDTO;
import com.peque.peque_backend.dtos.MensajeResponseDTO;
import com.peque.peque_backend.dtos.MfaRequestDTO;
import com.peque.peque_backend.dtos.RegistroRequestDTO;
import com.peque.peque_backend.services.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequestDTO request) {
        String resultado = authService.registrar(request);
        if (resultado.startsWith("Registro exitoso")) {
            return ResponseEntity.status(HttpStatus.CREATED).body(new MensajeResponseDTO(resultado));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeResponseDTO(resultado));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mfa")
    public ResponseEntity<LoginResponseDTO> verificarMfa(@RequestBody MfaRequestDTO request) {
        LoginResponseDTO response = authService.verificarMfa(request.getCorreo(), request.getCodigo());
        if (response.getMensaje().equals("Login exitoso")) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @GetMapping("/verify")
    public RedirectView verifyAccount(@RequestParam("token") String token) {
        try {
            String message = authService.verifyEmail(token);
            RedirectView redirectView = new RedirectView("http://localhost:4200/verificacion-exitosa");
            redirectView.setStatusCode(HttpStatus.FOUND);
            return redirectView;
        } catch (RuntimeException e) {
            RedirectView redirectView = new RedirectView("http://localhost:4200/verificacion-error");
            redirectView.setStatusCode(HttpStatus.FOUND);
            return redirectView;
        }
    }
}