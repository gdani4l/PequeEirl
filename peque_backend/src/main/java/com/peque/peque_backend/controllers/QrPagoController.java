package com.peque.peque_backend.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peque.peque_backend.dtos.NiubizSesionRequestDTO;
import com.peque.peque_backend.dtos.VentaRequestDTO;
import com.peque.peque_backend.dtos.VentaResponseDTO;
import com.peque.peque_backend.models.PagoNiubiz;
import com.peque.peque_backend.repositories.PagoNiubizRepository;
import com.peque.peque_backend.services.NiubizService;
import com.peque.peque_backend.services.VentaService;
import java.math.BigDecimal;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/niubiz/qr")
public class QrPagoController {

    private static final int EXPIRACION_MINUTOS = 10;

    @Autowired
    private PagoNiubizRepository pagoNiubizRepository;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private NiubizService niubizService;

    @Value("${niubiz.checkout-js-url:https://static-content-qas.vnforapps.com/v2/js/checkout.js?qa=true}")
    private String checkoutJsUrl;

    @Value("${app.qr.base-url:}")
    private String qrBaseUrl;

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

    private String obtenerBaseUrl() {
        if (qrBaseUrl != null && !qrBaseUrl.isBlank()) {
            return qrBaseUrl.replaceAll("/+$", "");
        }
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 53);
            return "http://" + socket.getLocalAddress().getHostAddress() + ":8080";
        } catch (Exception e) {
            try {
                return "http://" + InetAddress.getLocalHost().getHostAddress() + ":8080";
            } catch (Exception ex) {
                return "http://localhost:8080";
            }
        }
    }

    private boolean expirada(PagoNiubiz pago) {
        return pago.getFecha_creacion() != null
                && pago.getFecha_creacion().plusMinutes(EXPIRACION_MINUTOS).isBefore(LocalDateTime.now());
    }

    private String escapeHtml(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crear(@RequestBody NiubizSesionRequestDTO request,
            @RequestParam(value = "tipo", required = false, defaultValue = "yape") String tipo) {
        try {
            boolean tarjeta = "tarjeta".equalsIgnoreCase(tipo);
            if (request.getVenta() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "La venta es obligatoria");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            BigDecimal monto = ventaService.calcularTotalConIgv(request.getVenta().getDetalles());
            aplicarUsuarioDelToken(request.getVenta());
            Long purchaseNumber = System.currentTimeMillis() % 1_000_000_000_000L;

            PagoNiubiz pago = new PagoNiubiz();
            pago.setPurchase_number(purchaseNumber);
            pago.setMonto(monto);
            pago.setEstado("PENDIENTE");
            pago.setMarca(tarjeta ? "tarjeta-qr" : "yape-qr");
            pago.setVenta_json(objectMapper.writeValueAsString(request.getVenta()));
            pago.setFecha_creacion(LocalDateTime.now());
            pagoNiubizRepository.save(pago);

            String ruta = tarjeta ? "/api/niubiz/qr/checkout/" : "/api/niubiz/qr/pagina/";
            Map<String, Object> response = new HashMap<>();
            response.put("purchaseNumber", purchaseNumber);
            response.put("monto", monto);
            response.put("urlPago", obtenerBaseUrl() + ruta + purchaseNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "No se pudo generar el QR de pago");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping(value = "/checkout/{pn}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> checkout(@PathVariable Long pn) {
        Optional<PagoNiubiz> pagoOpt = pagoNiubizRepository.buscarPorPurchaseNumber(pn);
        if (pagoOpt.isEmpty()) {
            return ResponseEntity.ok(paginaMensaje("❌", "Orden no encontrada",
                    "Este código de pago no existe. Solicita uno nuevo en caja."));
        }

        PagoNiubiz pago = pagoOpt.get();
        if ("APROBADO".equals(pago.getEstado())) {
            return ResponseEntity.ok(paginaMensaje("✅", "Pago ya realizado",
                    "Esta orden ya fue pagada. ¡Gracias!"));
        }
        if (!"PENDIENTE".equals(pago.getEstado()) || expirada(pago)) {
            if ("PENDIENTE".equals(pago.getEstado())) {
                pago.setEstado("EXPIRADO");
                pago.setFecha_respuesta(LocalDateTime.now());
                pagoNiubizRepository.save(pago);
            }
            return ResponseEntity.ok(paginaMensaje("⏰", "Código expirado",
                    "Este código de pago ya no es válido. Solicita uno nuevo en caja."));
        }

        String email = "";
        try {
            JsonNode venta = objectMapper.readTree(pago.getVenta_json());
            email = venta.path("clienteEmail").asText("");
        } catch (Exception ignored) {
        }

        String sessionKey;
        try {
            sessionKey = niubizService.crearSesion(pago.getMonto(), email);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(paginaMensaje("❌", "No disponible",
                    "No se pudo iniciar el pago con Niubiz. Verifica la conexión a internet e intenta de nuevo."));
        }

        String base = obtenerBaseUrl();
        String monto = pago.getMonto().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Pagar con tarjeta - Peque POS</title>
                <style>
                  * { margin: 0; padding: 0; box-sizing: border-box; font-family: -apple-system, 'Segoe UI', Roboto, sans-serif; }
                  body { min-height: 100vh; background: linear-gradient(160deg, #1e3a8a, #2563eb); display: flex; align-items: center; justify-content: center; padding: 20px; }
                  .card { background: white; border-radius: 20px; padding: 32px 26px; width: 100%%; max-width: 380px; text-align: center; box-shadow: 0 20px 60px rgba(0,0,0,0.35); }
                  .logo { font-size: 1.3rem; font-weight: 800; color: #1e3a8a; margin-bottom: 4px; }
                  .demo { display: inline-block; background: #dbeafe; color: #1d4ed8; font-size: 0.65rem; font-weight: 700; padding: 2px 10px; border-radius: 12px; margin-bottom: 18px; letter-spacing: 0.05em; }
                  .comercio { color: #64748b; font-size: 0.85rem; }
                  .monto { font-size: 2.4rem; font-weight: 800; color: #1e293b; margin: 8px 0 20px; }
                  .nota { margin-top: 16px; font-size: 0.72rem; color: #94a3b8; line-height: 1.5; }
                </style>
                </head>
                <body>
                  <div class="card">
                    <div class="logo">Peque Eirl</div>
                    <div><span class="demo">Pagar con Niubiz - Tarjeta</span></div>
                    <div class="comercio">Total a pagar</div>
                    <div class="monto">S/ %s</div>
                    <form action="%s/api/niubiz/qr/respuesta?purchase=%d" method="post">
                      <script src="%s"
                        data-sessiontoken="%s"
                        data-channel="web"
                        data-merchantid="%s"
                        data-purchasenumber="%d"
                        data-amount="%s"
                        data-expirationminutes="15"
                        data-timeouturl="%s/api/niubiz/qr/checkout/%d/timeout"
                        data-merchantname="Peque POS"
                        data-formbuttoncolor="#2563eb"
                        data-cardholderemail="%s">
                      </script>
                    </form>
                    <div class="nota">Pulsa el botón "Pagar" para ingresar tu tarjeta en el formulario de Niubiz.</div>
                  </div>
                </body>
                </html>
                """
                .formatted(monto, base, pn, checkoutJsUrl, sessionKey, niubizService.getMerchantId(),
                        pn, monto, base, pn, escapeHtml(email));
        return ResponseEntity.ok(html);
    }

    @GetMapping(value = "/checkout/{pn}/timeout", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> checkoutTimeout(@PathVariable Long pn) {
        return ResponseEntity.ok(paginaMensaje("⏰", "Tiempo agotado",
                "El formulario de pago expiró. Vuelve a escanear el código QR o solicita uno nuevo en caja."));
    }

    @PostMapping(value = "/respuesta", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> respuestaCheckout(
            @RequestParam("purchase") Long purchaseNumber,
            @RequestParam(value = "transactionToken", required = false) String transactionToken) {

        Optional<PagoNiubiz> pagoOpt = pagoNiubizRepository.buscarPorPurchaseNumber(purchaseNumber);
        if (pagoOpt.isEmpty()) {
            return ResponseEntity.ok(paginaMensaje("❌", "Orden no encontrada",
                    "Este código de pago no existe. Solicita uno nuevo en caja."));
        }

        PagoNiubiz pago = pagoOpt.get();
        if ("APROBADO".equals(pago.getEstado())) {
            return ResponseEntity.ok(paginaMensaje("✅", "¡Pago realizado!",
                    "Esta orden ya fue pagada. ¡Gracias por tu compra!"));
        }

        pago.setFecha_respuesta(LocalDateTime.now());

        if (transactionToken == null || transactionToken.isBlank()) {
            pago.setEstado("RECHAZADO");
            pago.setDescripcion("El cliente canceló o no completó el pago");
            pagoNiubizRepository.save(pago);
            return ResponseEntity.ok(paginaMensaje("❌", "Pago no completado",
                    "No se completó el pago. Solicita un nuevo código QR en caja."));
        }

        pago.setTransaction_token(transactionToken);
        Map<String, String> resultado = niubizService.autorizarTransaccion(
                transactionToken, purchaseNumber, pago.getMonto());

        pago.setAction_code(resultado.get("actionCode"));
        pago.setDescripcion(resultado.get("descripcion"));
        pago.setTarjeta(resultado.get("tarjeta"));
        pago.setMarca(resultado.get("marca"));

        if (!"true".equals(resultado.get("aprobado"))) {
            pago.setEstado("RECHAZADO");
            pagoNiubizRepository.save(pago);
            return ResponseEntity.ok(paginaMensaje("❌", "Pago rechazado",
                    escapeHtml(resultado.get("descripcion")) + ". Solicita un nuevo código QR en caja."));
        }

        try {
            VentaRequestDTO ventaRequest = objectMapper.readValue(pago.getVenta_json(), VentaRequestDTO.class);
            String marca = resultado.get("marca") != null ? resultado.get("marca").toLowerCase() : "";
            ventaRequest.setMetodoPago(marca.contains("yape") ? "YAPE" : "TARJETA");
            VentaResponseDTO venta = ventaService.registrarVenta(ventaRequest);
            pago.setEstado("APROBADO");
            pago.setId_venta(venta.getIdVenta());
            pagoNiubizRepository.save(pago);
            return ResponseEntity.ok(paginaMensaje("✅", "¡Pago realizado!",
                    "Tu pago de S/ " + pago.getMonto().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
                            + " con tarjeta " + escapeHtml(pago.getTarjeta())
                            + " fue aprobado. Ya puedes cerrar esta página."));
        } catch (Exception e) {
            e.printStackTrace();
            pago.setEstado("APROBADO_SIN_VENTA");
            pagoNiubizRepository.save(pago);
            return ResponseEntity.ok(paginaMensaje("⚠️", "Pago aprobado con observación",
                    "El pago fue aprobado pero hubo un problema al registrar la venta. Acércate a caja."));
        }
    }

    @GetMapping(value = "/pagina/{pn}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> pagina(@PathVariable Long pn) {
        Optional<PagoNiubiz> pagoOpt = pagoNiubizRepository.buscarPorPurchaseNumber(pn);
        if (pagoOpt.isEmpty()) {
            return ResponseEntity.ok(paginaMensaje("❌", "Orden no encontrada",
                    "Este código de pago no existe. Solicita uno nuevo en caja."));
        }

        PagoNiubiz pago = pagoOpt.get();
        if ("APROBADO".equals(pago.getEstado())) {
            return ResponseEntity.ok(paginaMensaje("✅", "Pago ya realizado",
                    "Esta orden ya fue pagada. ¡Gracias!"));
        }
        if (!"PENDIENTE".equals(pago.getEstado()) || expirada(pago)) {
            if ("PENDIENTE".equals(pago.getEstado())) {
                pago.setEstado("EXPIRADO");
                pago.setFecha_respuesta(LocalDateTime.now());
                pagoNiubizRepository.save(pago);
            }
            return ResponseEntity.ok(paginaMensaje("⏰", "Código expirado",
                    "Este código de pago ya no es válido. Solicita uno nuevo en caja."));
        }

        String monto = pago.getMonto().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Pagar - Peque POS</title>
                <style>
                  * { margin: 0; padding: 0; box-sizing: border-box; font-family: -apple-system, 'Segoe UI', Roboto, sans-serif; }
                  body { min-height: 100vh; background: linear-gradient(160deg, #4c0e73, #7b1fa2); display: flex; align-items: center; justify-content: center; padding: 20px; }
                  .card { background: white; border-radius: 20px; padding: 32px 26px; width: 100%%; max-width: 360px; text-align: center; box-shadow: 0 20px 60px rgba(0,0,0,0.35); }
                  .logo { font-size: 1.4rem; font-weight: 800; color: #4c0e73; margin-bottom: 4px; }
                  .demo { display: inline-block; background: #fef3c7; color: #92400e; font-size: 0.65rem; font-weight: 700; padding: 2px 10px; border-radius: 12px; margin-bottom: 18px; letter-spacing: 0.05em; }
                  .comercio { color: #64748b; font-size: 0.85rem; }
                  .monto { font-size: 2.6rem; font-weight: 800; color: #1e293b; margin: 8px 0 22px; }
                  .btn { width: 100%%; background: #4c0e73; color: white; border: none; border-radius: 14px; padding: 15px; font-size: 1.05rem; font-weight: 700; cursor: pointer; }
                  .btn:active { opacity: 0.85; }
                  .nota { margin-top: 14px; font-size: 0.72rem; color: #94a3b8; }
                </style>
                </head>
                <body>
                  <div class="card">
                    <div class="logo">Pagar con Yape</div>
                    <div class="comercio">Estás pagando a</div>
                    <div class="comercio"><strong>Peque Eirl.</strong></div>
                    <div class="monto">S/ %s</div>
                    <form method="post" action="/api/niubiz/qr/confirmar/%d">
                      <button class="btn" type="submit">Pagar S/ %s</button>
                    </form>
                  </div>
                </body>
                </html>
                """
                .formatted(monto, pn, monto);
        return ResponseEntity.ok(html);
    }

    @PostMapping(value = "/confirmar/{pn}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> confirmar(@PathVariable Long pn) {
        Optional<PagoNiubiz> pagoOpt = pagoNiubizRepository.buscarPorPurchaseNumber(pn);
        if (pagoOpt.isEmpty()) {
            return ResponseEntity.ok(paginaMensaje("❌", "Orden no encontrada",
                    "Este código de pago no existe. Solicita uno nuevo en caja."));
        }

        PagoNiubiz pago = pagoOpt.get();
        if ("APROBADO".equals(pago.getEstado())) {
            return ResponseEntity.ok(paginaMensaje("✅", "¡Pago realizado!",
                    "Esta orden ya fue pagada. ¡Gracias por tu compra!"));
        }
        if (!"PENDIENTE".equals(pago.getEstado()) || expirada(pago)) {
            if ("PENDIENTE".equals(pago.getEstado())) {
                pago.setEstado("EXPIRADO");
                pago.setFecha_respuesta(LocalDateTime.now());
                pagoNiubizRepository.save(pago);
            }
            return ResponseEntity.ok(paginaMensaje("⏰", "Código expirado",
                    "Este código de pago ya no es válido. Solicita uno nuevo en caja."));
        }

        try {
            VentaRequestDTO ventaRequest = objectMapper.readValue(pago.getVenta_json(), VentaRequestDTO.class);
            ventaRequest.setMetodoPago("YAPE");
            VentaResponseDTO venta = ventaService.registrarVenta(ventaRequest);

            pago.setEstado("APROBADO");
            pago.setId_venta(venta.getIdVenta());
            pago.setDescripcion("Pago QR simulado aprobado");
            pago.setFecha_respuesta(LocalDateTime.now());
            pagoNiubizRepository.save(pago);

            return ResponseEntity.ok(paginaMensaje("✅", "¡Pago realizado!",
                    "Tu pago de S/ " + pago.getMonto().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
                            + " fue confirmado. Ya puedes cerrar esta página."));
        } catch (Exception e) {
            e.printStackTrace();
            pago.setEstado("RECHAZADO");
            pago.setDescripcion("Error al registrar la venta: " + e.getMessage());
            pago.setFecha_respuesta(LocalDateTime.now());
            pagoNiubizRepository.save(pago);
            return ResponseEntity.ok(paginaMensaje("❌", "No se pudo procesar",
                    "Ocurrió un problema al registrar la venta. Acércate a caja."));
        }
    }

    @GetMapping("/estado/{pn}")
    public ResponseEntity<?> estado(@PathVariable Long pn) {
        Optional<PagoNiubiz> pagoOpt = pagoNiubizRepository.buscarPorPurchaseNumber(pn);
        if (pagoOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Orden no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        PagoNiubiz pago = pagoOpt.get();
        if ("PENDIENTE".equals(pago.getEstado()) && expirada(pago)) {
            pago.setEstado("EXPIRADO");
            pago.setFecha_respuesta(LocalDateTime.now());
            pagoNiubizRepository.save(pago);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("estado", pago.getEstado());
        if ("APROBADO".equals(pago.getEstado())) {
            Map<String, Object> venta = new HashMap<>();
            venta.put("idVenta", pago.getId_venta());
            venta.put("total", pago.getMonto());
            response.put("venta", venta);
        }
        return ResponseEntity.ok(response);
    }

    private String paginaMensaje(String icono, String titulo, String detalle) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s - Peque POS</title>
                <style>
                  * { margin: 0; padding: 0; box-sizing: border-box; font-family: -apple-system, 'Segoe UI', Roboto, sans-serif; }
                  body { min-height: 100vh; background: linear-gradient(160deg, #4c0e73, #7b1fa2); display: flex; align-items: center; justify-content: center; padding: 20px; }
                  .card { background: white; border-radius: 20px; padding: 36px 26px; width: 100%%; max-width: 360px; text-align: center; box-shadow: 0 20px 60px rgba(0,0,0,0.35); }
                  .icono { font-size: 3rem; margin-bottom: 12px; }
                  h1 { font-size: 1.3rem; color: #1e293b; margin-bottom: 10px; }
                  p { font-size: 0.9rem; color: #64748b; line-height: 1.5; }
                </style>
                </head>
                <body>
                  <div class="card">
                    <div class="icono">%s</div>
                    <h1>%s</h1>
                    <p>%s</p>
                  </div>
                </body>
                </html>
                """
                .formatted(titulo, icono, titulo, detalle);
    }
}
