package com.peque.peque_backend.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.peque.peque_backend.models.Cliente;
import com.peque.peque_backend.models.DetalleVenta;
import com.peque.peque_backend.models.Venta;
import com.peque.peque_backend.repositories.DetalleVentaRepository;
import com.peque.peque_backend.repositories.VentaRepository;

@Service
public class MisVentasService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private PdfService pdfService;

    private boolean esAnulada(Venta v) {
        return v.getEstadoVenta() != null && "ANULADA".equals(v.getEstadoVenta().getNombre());
    }

    private String nombreCliente(Cliente c) {
        if (c == null) {
            return "Cliente varios";
        }
        if (c.getRazon_social() != null && !c.getRazon_social().isBlank()) {
            return c.getRazon_social();
        }
        String nombre = ((c.getNombre() != null ? c.getNombre() : "") + " "
                + (c.getApellido_pat() != null ? c.getApellido_pat() : "") + " "
                + (c.getApellido_mat() != null ? c.getApellido_mat() : "")).trim().replaceAll("\\s+", " ");
        return nombre.isBlank() ? "Cliente varios" : nombre;
    }

    public List<Map<String, Object>> listarMisVentas(Integer idUsuario) {
        List<Venta> ventas = ventaRepository.findByUsuarioId(idUsuario);
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Venta v : ventas) {
            Map<String, Object> item = new HashMap<>();
            item.put("idVenta", v.getId_venta());
            item.put("comprobante", v.getSerie() + "-" + String.format("%08d", v.getNumero()));
            item.put("fecha", v.getFecha_venta());
            item.put("total", v.getTotal_pago());
            item.put("metodoPago", v.getMetodo_pago() != null ? v.getMetodo_pago().name() : "EFECTIVO");
            item.put("tipoComprobante", v.getTipo_comprobante() != null ? v.getTipo_comprobante().name() : "BOLETA");
            item.put("estado", v.getEstadoVenta() != null ? v.getEstadoVenta().getNombre() : "PENDIENTE");
            item.put("cliente", nombreCliente(v.getCliente()));
            resultado.add(item);
        }
        return resultado;
    }

    public Map<String, Object> resumen(Integer idUsuario) {
        List<Venta> ventas = ventaRepository.findByUsuarioId(idUsuario);

        List<Venta> validas = new ArrayList<>();
        for (Venta v : ventas) {
            if (!esAnulada(v)) {
                validas.add(v);
            }
        }

        int totalVentas = validas.size();
        BigDecimal totalIngresos = BigDecimal.ZERO;
        for (Venta v : validas) {
            totalIngresos = totalIngresos.add(v.getTotal_pago());
        }
        BigDecimal ticket = totalVentas > 0
                ? totalIngresos.divide(BigDecimal.valueOf(totalVentas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        LocalDate hoy = LocalDate.now();
        int ventasHoy = 0;
        BigDecimal ingresosHoy = BigDecimal.ZERO;
        for (Venta v : validas) {
            if (v.getFecha_venta() != null && v.getFecha_venta().toLocalDate().equals(hoy)) {
                ventasHoy++;
                ingresosHoy = ingresosHoy.add(v.getTotal_pago());
            }
        }

        Map<String, Object> r = new HashMap<>();
        r.put("totalVentas", totalVentas);
        r.put("totalIngresos", totalIngresos.setScale(2, RoundingMode.HALF_UP));
        r.put("ticketPromedio", ticket);
        r.put("ventasHoy", ventasHoy);
        r.put("ingresosHoy", ingresosHoy.setScale(2, RoundingMode.HALF_UP));
        r.put("ventasAnuladas", ventas.size() - validas.size());
        return r;
    }

    public byte[] comprobanteDelPropietario(Integer idVenta, Integer idUsuario) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        if (venta.getUsuario() == null || !venta.getUsuario().getId_usuario().equals(idUsuario)) {
            throw new SecurityException("No autorizado para descargar este comprobante");
        }
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(idVenta);
        return pdfService.generarComprobantePDF(venta, detalles);
    }
}
