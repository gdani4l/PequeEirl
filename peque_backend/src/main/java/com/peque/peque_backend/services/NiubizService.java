package com.peque.peque_backend.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class NiubizService {

    @Value("${niubiz.user}")
    private String user;

    @Value("${niubiz.password}")
    private String password;

    @Value("${niubiz.merchant-id}")
    private String merchantId;

    @Value("${niubiz.base-url}")
    private String baseUrl;

    @Value("${niubiz.yape-token-url}")
    private String yapeTokenUrl;

    @Value("${niubiz.yape-auth-channel}")
    private String yapeAuthChannel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getMerchantId() {
        return merchantId;
    }

    public String obtenerTokenAcceso() {
        String credenciales = Base64.getEncoder()
                .encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + credenciales);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/api.security/v1/security", HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/api.security/v1/security", HttpMethod.GET, entity, String.class);
            return response.getBody();
        }
    }

    public String crearSesion(BigDecimal monto, String clienteEmail) {
        String token = obtenerTokenAcceso();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        Map<String, Object> mdd = new HashMap<>();
        mdd.put("MDD4", clienteEmail != null && !clienteEmail.isBlank() ? clienteEmail : "cliente@peque.com");
        mdd.put("MDD21", 0);
        mdd.put("MDD32", "peque-pos");
        mdd.put("MDD75", "Registrado");
        mdd.put("MDD77", 1);

        Map<String, Object> antifraud = new HashMap<>();
        antifraud.put("clientIp", "127.0.0.1");
        antifraud.put("merchantDefineData", mdd);

        Map<String, Object> body = new HashMap<>();
        body.put("channel", "web");
        body.put("amount", monto.setScale(2, RoundingMode.HALF_UP));
        body.put("antifraud", antifraud);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api.ecommerce/v2/ecommerce/token/session/" + merchantId, entity, String.class);

        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            return json.get("sessionKey").asText();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear la sesión de Niubiz");
        }
    }

    public Map<String, String> autorizarTransaccion(String transactionToken, Long purchaseNumber, BigDecimal monto) {
        return autorizarConCanal(transactionToken, purchaseNumber, monto, "web");
    }

    public Map<String, String> autorizarTransaccionYape(String transactionToken, Long purchaseNumber, BigDecimal monto) {
        return autorizarConCanal(transactionToken, purchaseNumber, monto, yapeAuthChannel);
    }

    private Map<String, String> autorizarConCanal(String transactionToken, Long purchaseNumber, BigDecimal monto,
            String canal) {
        Map<String, String> resultado = new HashMap<>();
        try {
            String token = obtenerTokenAcceso();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", token);

            Map<String, Object> order = new HashMap<>();
            order.put("tokenId", transactionToken);
            order.put("purchaseNumber", String.valueOf(purchaseNumber));
            order.put("amount", monto.setScale(2, RoundingMode.HALF_UP));
            order.put("currency", "PEN");

            Map<String, Object> body = new HashMap<>();
            body.put("channel", canal);
            body.put("captureType", "manual");
            body.put("countable", true);
            body.put("order", order);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            System.out.println("[NIUBIZ AUTH " + canal + "] request=" + objectMapper.writeValueAsString(body));
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/api.authorization/v3/authorization/ecommerce/" + merchantId, entity, String.class);
            System.out.println("[NIUBIZ AUTH " + canal + "] status=" + response.getStatusCode()
                    + " body=" + response.getBody());

            JsonNode json = objectMapper.readTree(response.getBody());
            JsonNode dataMap = json.path("dataMap");
            resultado.put("actionCode", dataMap.path("ACTION_CODE").asText(""));
            resultado.put("descripcion", dataMap.path("ACTION_DESCRIPTION").asText(""));
            resultado.put("tarjeta", dataMap.path("CARD").asText(""));
            resultado.put("marca", dataMap.path("BRAND").asText(""));
            resultado.put("yapeId", dataMap.path("YAPE_ID").asText(""));
            resultado.put("aprobado", "000".equals(dataMap.path("ACTION_CODE").asText("")) ? "true" : "false");
        } catch (HttpStatusCodeException e) {
            System.err.println("[NIUBIZ AUTH " + canal + "] status=" + e.getStatusCode() + " body=" + e.getResponseBodyAsString());
            resultado.put("aprobado", "false");
            resultado.put("actionCode", "");
            resultado.put("descripcion", extraerDescripcionError(e.getResponseBodyAsString()));
            resultado.put("tarjeta", "");
            resultado.put("marca", "");
        } catch (Exception e) {
            resultado.put("aprobado", "false");
            resultado.put("actionCode", "");
            resultado.put("descripcion", "Error de comunicación con Niubiz");
            resultado.put("tarjeta", "");
            resultado.put("marca", "");
        }
        return resultado;
    }

    public Map<String, String> generarTokenYape(String celular, String otp, BigDecimal monto) {
        Map<String, String> resultado = new HashMap<>();
        try {
            String token = obtenerTokenAcceso();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", token);

            Map<String, Object> body = new HashMap<>();
            body.put("phoneNumber", celular);
            body.put("otp", otp);
            body.put("amount", monto.setScale(2, RoundingMode.HALF_UP));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            System.out.println("[NIUBIZ YAPE] POST " + yapeTokenUrl + merchantId);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    yapeTokenUrl + merchantId, entity, String.class);
            System.out.println("[NIUBIZ YAPE] respuesta=" + response.getBody());

            JsonNode json = objectMapper.readTree(response.getBody());
            String tokenYape = extraerTokenYape(json);
            if (tokenYape == null || tokenYape.isBlank()) {
                resultado.put("exito", "false");
                resultado.put("descripcion", "Niubiz no devolvió un token Yape. Respuesta: " + response.getBody());
                return resultado;
            }
            resultado.put("exito", "true");
            resultado.put("token", tokenYape);
            return resultado;
        } catch (HttpStatusCodeException e) {
            System.err.println("[NIUBIZ YAPE] status=" + e.getStatusCode() + " body=" + e.getResponseBodyAsString());
            resultado.put("exito", "false");
            resultado.put("descripcion", extraerDescripcionError(e.getResponseBodyAsString()));
            return resultado;
        } catch (Exception e) {
            System.err.println("[NIUBIZ YAPE] error=" + e.getMessage());
            resultado.put("exito", "false");
            resultado.put("descripcion", "Error de comunicación con Niubiz");
            return resultado;
        }
    }

    private String extraerTokenYape(JsonNode json) {
        String[] campos = { "transactionToken", "yapeTrxToken", "tokenId", "token" };
        for (String campo : campos) {
            if (json.hasNonNull(campo)) {
                return json.get(campo).asText();
            }
        }
        JsonNode data = json.path("data");
        for (String campo : campos) {
            if (data.hasNonNull(campo)) {
                return data.get(campo).asText();
            }
        }
        return null;
    }

    private String extraerDescripcionError(String cuerpo) {
        try {
            JsonNode json = objectMapper.readTree(cuerpo);
            String desc = json.path("data").path("ACTION_DESCRIPTION").asText("");
            if (desc.isBlank()) {
                desc = json.path("dataMap").path("ACTION_DESCRIPTION").asText("");
            }
            if (desc.isBlank()) {
                desc = json.path("errorMessage").asText("");
            }
            return desc.isBlank() ? "Transacción rechazada" : desc;
        } catch (Exception e) {
            return "Transacción rechazada";
        }
    }
}
