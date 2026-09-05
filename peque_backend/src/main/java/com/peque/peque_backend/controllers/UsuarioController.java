package com.peque.peque_backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.peque.peque_backend.dtos.MensajeResponseDTO;
import com.peque.peque_backend.dtos.UsuarioResponseDTO;
import com.peque.peque_backend.dtos.UsuarioUpdateDTO;
import com.peque.peque_backend.services.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> getUsuarios() {
        List<UsuarioResponseDTO> usuarios = usuarioService.getUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> getUsuario(@PathVariable Integer id) {
        UsuarioResponseDTO usuario = usuarioService.getUsuarioById(id);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MensajeResponseDTO> actualizarUsuario(@PathVariable Integer id,
            @RequestBody UsuarioUpdateDTO updateDTO) {
        String resultado = usuarioService.actualizarUsuario(id, updateDTO);
        return ResponseEntity.ok(new MensajeResponseDTO(resultado));
    }
}