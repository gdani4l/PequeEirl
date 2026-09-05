package com.peque.peque_backend.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.peque.peque_backend.services.AsesorIaService;
import com.peque.peque_backend.services.ReporteService;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private AsesorIaService asesorIaService;

    @GetMapping("/ventas")
    public ResponseEntity<List<Map<String, Object>>> getVentas(
            @RequestParam String periodo,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(reporteService.getVentas(periodo, mes, anio));
    }

    @GetMapping("/top-vendedores")
    public ResponseEntity<List<Map<String, Object>>> getTopVendedores() {
        return ResponseEntity.ok(reporteService.getTopVendedores());
    }

    @GetMapping("/top-productos")
    public ResponseEntity<List<Map<String, Object>>> getTopProductos(
            @RequestParam(required = false, defaultValue = "8") Integer limite) {
        int limiteSeguro = Math.max(1, Math.min(limite == null ? 8 : limite, 50));
        return ResponseEntity.ok(reporteService.getTopProductos(limiteSeguro));
    }

    @GetMapping("/hora-punta")
    public ResponseEntity<List<Map<String, Object>>> getHoraPunta() {
        return ResponseEntity.ok(reporteService.getHoraPunta());
    }

    @GetMapping("/distribucion")
    public ResponseEntity<Map<String, Object>> getDistribucion() {
        return ResponseEntity.ok(reporteService.getDistribucion());
    }

    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> getResumen() {
        return ResponseEntity.ok(reporteService.getResumen());
    }

    @GetMapping("/asesor")
    public ResponseEntity<Map<String, Object>> getAsesor() {
        return ResponseEntity.ok(asesorIaService.generarRecomendaciones());
    }
}