package com.peque.peque_backend.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.peque.peque_backend.dtos.MensajeResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MensajeResponseDTO> handleValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Los datos enviados no son válidos");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeResponseDTO(mensaje));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MensajeResponseDTO> handleIntegridad(DataIntegrityViolationException ex) {
        String detalle = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";
        String mensaje = "No se pudo guardar: el dato entra en conflicto con un registro existente";
        if (detalle != null && detalle.toLowerCase().contains("correo")) {
            mensaje = "El correo ya está en uso";
        } else if (detalle != null && detalle.toLowerCase().contains("duplicate")) {
            mensaje = "El dato ingresado ya está registrado";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeResponseDTO(mensaje));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MensajeResponseDTO> handleBodyIlegible(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeResponseDTO("Los datos enviados no tienen el formato esperado"));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<MensajeResponseDTO> handleSeguridad(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MensajeResponseDTO("No tienes permiso para realizar esta acción"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MensajeResponseDTO> handleRuntime(RuntimeException ex) {
        String mensaje = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : "Ocurrió un error al procesar la solicitud";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeResponseDTO(mensaje));
    }
}
