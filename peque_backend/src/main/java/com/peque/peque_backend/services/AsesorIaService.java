package com.peque.peque_backend.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peque.peque_backend.models.Producto;
import com.peque.peque_backend.repositories.ProductoRepository;

@Service
public class AsesorIaService {

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private ProductoRepository productoRepository;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> generarRecomendaciones() {
        Map<String, Object> resultado = new HashMap<>();
        try {
            String contexto = construirContexto();
            String prompt = construirPrompt(contexto);
            String textoJson = llamarGemini(prompt);

            JsonNode analisis = objectMapper.readTree(textoJson);
            resultado.put("resumen", analisis.path("resumen").asText(""));

            List<Map<String, Object>> recomendaciones = new ArrayList<>();
            for (JsonNode rec : analisis.path("recomendaciones")) {
                Map<String, Object> item = new HashMap<>();
                item.put("titulo", rec.path("titulo").asText(""));
                item.put("detalle", rec.path("detalle").asText(""));
                item.put("impacto", rec.path("impacto").asText("medio"));
                recomendaciones.add(item);
            }
            resultado.put("recomendaciones", recomendaciones);
            return resultado;
        } catch (HttpStatusCodeException e) {
            System.err.println("[ASESOR IA] status=" + e.getStatusCode() + " body=" + e.getResponseBodyAsString());
            resultado.put("error", "No se pudo generar el análisis. Verifica la API key de Gemini.");
            return resultado;
        } catch (Exception e) {
            System.err.println("[ASESOR IA] error=" + e.getMessage());
            resultado.put("error", "No se pudo generar el análisis en este momento.");
            return resultado;
        }
    }

    private String construirContexto() throws Exception {
        Map<String, Object> datos = new HashMap<>();
        datos.put("resumen", reporteService.getResumen());
        datos.put("topProductos", reporteService.getTopProductos(8));
        datos.put("topVendedores", reporteService.getTopVendedores());
        datos.put("distribucionPagos", reporteService.getDistribucion());

        List<Map<String, Object>> horas = reporteService.getHoraPunta();
        Map<String, Object> horaTop = null;
        for (Map<String, Object> h : horas) {
            int ventas = ((Number) h.get("ventas")).intValue();
            if (ventas == 0) {
                continue;
            }
            if (horaTop == null || ventas > ((Number) horaTop.get("ventas")).intValue()) {
                horaTop = h;
            }
        }
        datos.put("horaPunta", horaTop);

        List<Map<String, Object>> stockBajo = new ArrayList<>();
        for (Producto p : productoRepository.findAll()) {
            if (p.getStock() != null && p.getStock().compareTo(new BigDecimal("5")) <= 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("nombre", p.getNombre());
                item.put("stock", p.getStock());
                stockBajo.add(item);
            }
        }
        datos.put("stockBajo", stockBajo);

        return objectMapper.writeValueAsString(datos);
    }

    private String construirPrompt(String contexto) {
        return "Eres un asesor de negocios experto en retail para una tienda (punto de venta) en Perú. "
                + "Analiza los siguientes datos reales de ventas (los montos están en soles peruanos, S/) y genera "
                + "recomendaciones concretas y accionables para AUMENTAR LAS GANANCIAS del negocio. "
                + "Usa los nombres reales de los productos y las horas reales que aparecen en los datos. "
                + "Prioriza acciones sobre los productos más vendidos, la hora punta y los productos con stock bajo. "
                + "Entrega entre 3 y 5 recomendaciones. Responde ÚNICAMENTE en JSON con este formato exacto: "
                + "{\"resumen\": \"una frase breve de diagnóstico del negocio\", "
                + "\"recomendaciones\": [{\"titulo\": \"título corto\", "
                + "\"detalle\": \"explicación accionable en 1 o 2 frases\", "
                + "\"impacto\": \"alto|medio|bajo\"}]}. "
                + "No incluyas ningún texto fuera del JSON.\n\nDATOS DEL NEGOCIO:\n" + contexto;
    }

    private String llamarGemini(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        Map<String, Object> parte = new HashMap<>();
        parte.put("text", prompt);
        Map<String, Object> contenido = new HashMap<>();
        contenido.put("parts", List.of(parte));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.put("temperature", 0.7);

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(contenido));
        body.put("generationConfig", generationConfig);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String url = baseUrl + "/models/" + model + ":generateContent";
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            return json.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
        } catch (Exception e) {
            throw new RuntimeException("Respuesta inválida de Gemini");
        }
    }
}
