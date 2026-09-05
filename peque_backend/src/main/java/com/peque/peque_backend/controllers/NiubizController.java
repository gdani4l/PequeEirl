package com.peque.peque_backend.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peque.peque_backend.dtos.NiubizSesionRequestDTO;
import com.peque.peque_backend.dtos.NiubizYapeRequestDTO;
import com.peque.peque_backend.dtos.VentaRequestDTO;
import com.peque.peque_backend.dtos.VentaResponseDTO;
import com.peque.peque_backend.models.PagoNiubiz;
import com.peque.peque_backend.repositories.PagoNiubizRepository;
import com.peque.peque_backend.services.NiubizService;
import com.peque.peque_backend.services.VentaService;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/niubiz")
public class NiubizController {

    @Autowired
    private NiubizService niubizService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private PagoNiubizRepository pagoNiubizRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private void aplicarUsuarioDelToken(VentaRequestDTO venta) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (venta != null && auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) {
            try {
                venta.setIdUsuario(Integer.valueOf(auth.getName()));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @PostMapping("/sesion")
    public ResponseEntity<?> crearSesion(@RequestBody NiubizSesionRequestDTO request) {
        try {
            if (request.getVenta() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "La venta es obligatoria");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            BigDecimal monto = ventaService.calcularTotalConIgv(request.getVenta().getDetalles());
            String email = request.getVenta().getClienteEmail() != null
                    ? request.getVenta().getClienteEmail()
                    : "";

            aplicarUsuarioDelToken(request.getVenta());
            String sessionKey = niubizService.crearSesion(monto, email);
            Long purchaseNumber = System.currentTimeMillis() % 1_000_000_000_000L;

            PagoNiubiz pago = new PagoNiubiz();
            pago.setPurchase_number(purchaseNumber);
            pago.setMonto(monto);
            pago.setEstado("PENDIENTE");
            pago.setVenta_json(objectMapper.writeValueAsString(request.getVenta()));
            pago.setFecha_creacion(LocalDateTime.now());
            pagoNiubizRepository.save(pago);

            Map<String, Object> response = new HashMap<>();
            response.put("sessionKey", sessionKey);
            response.put("merchantId", niubizService.getMerchantId());
            response.put("purchaseNumber", purchaseNumber);
            response.put("monto", monto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "No se pudo iniciar el pago con Niubiz");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/pagar-yape")
    public ResponseEntity<?> pagarYape(@RequestBody NiubizYapeRequestDTO request) {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            if (request.getVenta() == null) {
                respuesta.put("estado", "error");
                respuesta.put("descripcion", "La venta es obligatoria");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
            }
            BigDecimal monto = ventaService.calcularTotalConIgv(request.getVenta().getDetalles());
            aplicarUsuarioDelToken(request.getVenta());
            Long purchaseNumber = System.currentTimeMillis() % 1_000_000_000_000L;

            PagoNiubiz pago = new PagoNiubiz();
            pago.setPurchase_number(purchaseNumber);
            pago.setMonto(monto);
            pago.setEstado("PENDIENTE");
            pago.setVenta_json(objectMapper.writeValueAsString(request.getVenta()));
            pago.setFecha_creacion(LocalDateTime.now());
            pagoNiubizRepository.save(pago);

            Map<String, String> tokenYape = niubizService.generarTokenYape(
                    request.getCelular(), request.getOtp(), monto);

            if (!"true".equals(tokenYape.get("exito"))) {
                pago.setEstado("RECHAZADO");
                pago.setDescripcion(tokenYape.get("descripcion"));
                pago.setFecha_respuesta(LocalDateTime.now());
                pagoNiubizRepository.save(pago);
                respuesta.put("estado", "rechazado");
                respuesta.put("descripcion", tokenYape.get("descripcion"));
                return ResponseEntity.ok(respuesta);
            }

            pago.setTransaction_token(tokenYape.get("token"));
            Map<String, String> resultado = niubizService.autorizarTransaccionYape(
                    tokenYape.get("token"), purchaseNumber, monto);

            pago.setAction_code(resultado.get("actionCode"));
            pago.setDescripcion(resultado.get("descripcion"));
            pago.setTarjeta(resultado.get("tarjeta"));
            pago.setMarca("yape");
            pago.setFecha_respuesta(LocalDateTime.now());

            if (!"true".equals(resultado.get("aprobado"))) {
                pago.setEstado("RECHAZADO");
                pagoNiubizRepository.save(pago);
                respuesta.put("estado", "rechazado");
                respuesta.put("descripcion", resultado.get("descripcion"));
                return ResponseEntity.ok(respuesta);
            }

            VentaRequestDTO ventaRequest = objectMapper.readValue(pago.getVenta_json(), VentaRequestDTO.class);
            ventaRequest.setMetodoPago("YAPE");
            VentaResponseDTO venta = ventaService.registrarVenta(ventaRequest);
            pago.setEstado("APROBADO");
            pago.setId_venta(venta.getIdVenta());
            pagoNiubizRepository.save(pago);

            Map<String, Object> ventaMap = new HashMap<>();
            ventaMap.put("idVenta", venta.getIdVenta());
            ventaMap.put("serie", venta.getSerie());
            ventaMap.put("numero", venta.getNumero());
            ventaMap.put("total", venta.getTotal());

            respuesta.put("estado", "aprobado");
            respuesta.put("venta", ventaMap);
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            e.printStackTrace();
            respuesta.put("estado", "error");
            respuesta.put("descripcion", "No se pudo procesar el pago con Yape");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }

    @PostMapping("/respuesta")
    public ResponseEntity<Void> respuesta(
            @RequestParam("purchase") Long purchaseNumber,
            @RequestParam(value = "transactionToken", required = false) String transactionToken) {

        System.out.println("[NIUBIZ RESPUESTA] purchase=" + purchaseNumber
                + " transactionToken=" + (transactionToken == null ? "null" : (transactionToken.isBlank() ? "(vacío)" : "presente")));

        Optional<PagoNiubiz> pagoOpt = pagoNiubizRepository.buscarPorPurchaseNumber(purchaseNumber);
        if (pagoOpt.isEmpty()) {
            return redirigir("estado=error&descripcion=" + codificar("Orden de pago no encontrada"));
        }

        PagoNiubiz pago = pagoOpt.get();
        pago.setFecha_respuesta(LocalDateTime.now());

        if (transactionToken == null || transactionToken.isBlank()) {
            pago.setEstado("RECHAZADO");
            pago.setDescripcion("El cliente canceló o no completó el pago");
            pagoNiubizRepository.save(pago);
            return redirigir("estado=rechazado&descripcion=" + codificar("El pago no se completó"));
        }

        pago.setTransaction_token(transactionToken);
        Map<String, String> resultado = niubizService.autorizarTransaccion(
                transactionToken, purchaseNumber, pago.getMonto());
        System.out.println("[NIUBIZ RESPUESTA] resultado=" + resultado);

        pago.setAction_code(resultado.get("actionCode"));
        pago.setDescripcion(resultado.get("descripcion"));
        pago.setTarjeta(resultado.get("tarjeta"));
        pago.setMarca(resultado.get("marca"));

        if (!"true".equals(resultado.get("aprobado"))) {
            pago.setEstado("RECHAZADO");
            pagoNiubizRepository.save(pago);
            return redirigir("estado=rechazado&descripcion=" + codificar(resultado.get("descripcion")));
        }

        try {
            VentaRequestDTO ventaRequest = objectMapper.readValue(pago.getVenta_json(), VentaRequestDTO.class);
            String marca = resultado.get("marca") != null ? resultado.get("marca").toLowerCase() : "";
            ventaRequest.setMetodoPago(marca.contains("yape") ? "YAPE" : "TARJETA");
            VentaResponseDTO venta = ventaService.registrarVenta(ventaRequest);
            pago.setEstado("APROBADO");
            pago.setId_venta(venta.getIdVenta());
            pagoNiubizRepository.save(pago);
            return redirigir("estado=aprobado"
                    + "&serie=" + codificar(venta.getSerie())
                    + "&numero=" + venta.getNumero()
                    + "&monto=" + pago.getMonto()
                    + "&tarjeta=" + codificar(pago.getTarjeta())
                    + "&marca=" + codificar(pago.getMarca()));
        } catch (Exception e) {
            e.printStackTrace();
            pago.setEstado("APROBADO_SIN_VENTA");
            pagoNiubizRepository.save(pago);
            return redirigir("estado=error&descripcion="
                    + codificar("El pago fue aprobado pero hubo un error al registrar la venta. Contacte al administrador."));
        }
    }

    private ResponseEntity<Void> redirigir(String queryString) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(frontendUrl + "/pago-resultado?" + queryString))
                .build();
    }

    private String codificar(String valor) {
        return URLEncoder.encode(valor == null ? "" : valor, StandardCharsets.UTF_8);
    }
}
