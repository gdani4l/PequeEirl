package com.peque.peque_backend.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.peque.peque_backend.models.Venta;
import com.peque.peque_backend.repositories.DetalleVentaRepository;
import com.peque.peque_backend.repositories.VentaRepository;

@Service
public class ReporteService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    public List<Map<String, Object>> getVentas(String periodo, Integer mes, Integer anio) {
        if (periodo.equals("dia")) {
            return getVentasDia();
        } else if (periodo.equals("semana")) {
            return getVentasSemana();
        } else if (periodo.equals("mes")) {
            return getVentasMes(mes, anio);
        }
        return new ArrayList<>();
    }

    private List<Map<String, Object>> getVentasDia() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDate.now().atTime(23, 59, 59);

        List<Venta> ventas = ventaRepository.findVentasByFechaBetween(inicio, fin);

        Map<Integer, Integer> ventasPorHora = new HashMap<>();
        Map<Integer, BigDecimal> montoPorHora = new HashMap<>();

        for (int i = 0; i < 24; i++) {
            ventasPorHora.put(i, 0);
            montoPorHora.put(i, BigDecimal.ZERO);
        }

        for (Venta v : ventas) {
            int hora = v.getFecha_venta().getHour();
            Integer count = ventasPorHora.get(hora);
            if (count == null) {
                count = 0;
            }
            ventasPorHora.put(hora, count + 1);

            BigDecimal monto = montoPorHora.get(hora);
            if (monto == null) {
                monto = BigDecimal.ZERO;
            }
            montoPorHora.put(hora, monto.add(v.getTotal_pago()));
        }

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("label", String.format("%02d:00", i));
            item.put("ventas", ventasPorHora.getOrDefault(i, 0));
            item.put("monto", montoPorHora.getOrDefault(i, BigDecimal.ZERO));
            resultado.add(item);
        }

        return resultado;
    }

    private List<Map<String, Object>> getVentasSemana() {
        LocalDateTime hoy = LocalDateTime.now();
        LocalDateTime inicioSemana = hoy.with(java.time.DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0)
                .withNano(0);

        List<Venta> ventas = ventaRepository.findVentasByFechaBetween(inicioSemana, hoy);

        Map<String, Integer> ventasPorDia = new HashMap<>();
        Map<String, BigDecimal> montoPorDia = new HashMap<>();

        String[] dias = { "Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom" };
        for (String dia : dias) {
            ventasPorDia.put(dia, 0);
            montoPorDia.put(dia, BigDecimal.ZERO);
        }

        for (Venta v : ventas) {
            String nombreDia = v.getFecha_venta().getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT,
                    new Locale("es"));
            nombreDia = nombreDia.substring(0, 1).toUpperCase() + nombreDia.substring(1);

            Integer count = ventasPorDia.get(nombreDia);
            if (count == null) {
                count = 0;
            }
            ventasPorDia.put(nombreDia, count + 1);

            BigDecimal monto = montoPorDia.get(nombreDia);
            if (monto == null) {
                monto = BigDecimal.ZERO;
            }
            montoPorDia.put(nombreDia, monto.add(v.getTotal_pago()));
        }

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (String dia : dias) {
            Map<String, Object> item = new HashMap<>();
            item.put("label", dia);
            item.put("ventas", ventasPorDia.getOrDefault(dia, 0));
            item.put("monto", montoPorDia.getOrDefault(dia, BigDecimal.ZERO));
            resultado.add(item);
        }

        return resultado;
    }

    private List<Map<String, Object>> getVentasMes(Integer mes, Integer anio) {
        if (mes == null || anio == null) {
            LocalDate ahora = LocalDate.now();
            mes = ahora.getMonthValue();
            anio = ahora.getYear();
        }

        LocalDateTime inicio = LocalDate.of(anio, mes, 1).atStartOfDay();
        LocalDateTime fin = LocalDate.of(anio, mes, 1).plusMonths(1).minusDays(1).atTime(23, 59, 59);

        List<Venta> ventas = ventaRepository.findVentasByFechaBetween(inicio, fin);

        Map<Integer, Integer> ventasPorDia = new HashMap<>();
        Map<Integer, BigDecimal> montoPorDia = new HashMap<>();

        int diasDelMes = LocalDate.of(anio, mes, 1).lengthOfMonth();
        for (int i = 1; i <= diasDelMes; i++) {
            ventasPorDia.put(i, 0);
            montoPorDia.put(i, BigDecimal.ZERO);
        }

        for (Venta v : ventas) {
            int dia = v.getFecha_venta().getDayOfMonth();
            Integer count = ventasPorDia.get(dia);
            if (count == null) {
                count = 0;
            }
            ventasPorDia.put(dia, count + 1);

            BigDecimal monto = montoPorDia.get(dia);
            if (monto == null) {
                monto = BigDecimal.ZERO;
            }
            montoPorDia.put(dia, monto.add(v.getTotal_pago()));
        }

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (int i = 1; i <= diasDelMes; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("label", String.valueOf(i));
            item.put("ventas", ventasPorDia.getOrDefault(i, 0));
            item.put("monto", montoPorDia.getOrDefault(i, BigDecimal.ZERO));
            resultado.add(item);
        }

        return resultado;
    }

    public List<Map<String, Object>> getTopVendedores() {
        List<Object[]> resultados = detalleVentaRepository.findTopVendedores();

        List<Map<String, Object>> topVendedores = new ArrayList<>();
        for (Object[] row : resultados) {
            Map<String, Object> item = new HashMap<>();
            item.put("nombre", row[0].toString());
            item.put("ventas", ((Number) row[1]).intValue());
            item.put("monto", (BigDecimal) row[2]);
            topVendedores.add(item);
        }

        return topVendedores;
    }

    public List<Map<String, Object>> getTopProductos(int limite) {
        List<com.peque.peque_backend.models.DetalleVenta> detalles = detalleVentaRepository.findAll();

        Map<String, BigDecimal> unidadesPorProducto = new LinkedHashMap<>();
        Map<String, BigDecimal> montoPorProducto = new LinkedHashMap<>();

        for (com.peque.peque_backend.models.DetalleVenta d : detalles) {
            if (esAnulada(d.getVenta())) {
                continue;
            }
            String nombre = d.getProducto().getNombre();
            BigDecimal unidades = unidadesPorProducto.getOrDefault(nombre, BigDecimal.ZERO);
            unidadesPorProducto.put(nombre, unidades.add(d.getCantidad()));
            BigDecimal monto = montoPorProducto.getOrDefault(nombre, BigDecimal.ZERO);
            montoPorProducto.put(nombre, monto.add(d.getCantidad().multiply(d.getPrecio_fijo())));
        }

        return unidadesPorProducto.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limite)
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("nombre", e.getKey());
                    item.put("unidades", e.getValue());
                    item.put("monto", montoPorProducto.get(e.getKey()).setScale(2, RoundingMode.HALF_UP));
                    return item;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Map<String, Object>> getHoraPunta() {
        List<Venta> ventas = ventaRepository.findAll();

        int[] cantidadPorHora = new int[24];
        BigDecimal[] montoPorHora = new BigDecimal[24];
        for (int i = 0; i < 24; i++) {
            montoPorHora[i] = BigDecimal.ZERO;
        }

        for (Venta v : ventas) {
            if (esAnulada(v)) {
                continue;
            }
            int hora = v.getFecha_venta().getHour();
            cantidadPorHora[hora]++;
            montoPorHora[hora] = montoPorHora[hora].add(v.getTotal_pago());
        }

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("label", String.format("%02d:00", i));
            item.put("ventas", cantidadPorHora[i]);
            item.put("monto", montoPorHora[i].setScale(2, RoundingMode.HALF_UP));
            resultado.add(item);
        }
        return resultado;
    }

    public Map<String, Object> getDistribucion() {
        List<Venta> ventas = ventaRepository.findAll();

        Map<String, Integer> porMetodo = new LinkedHashMap<>();
        Map<String, BigDecimal> montoPorMetodo = new LinkedHashMap<>();
        Map<String, Integer> porComprobante = new LinkedHashMap<>();

        for (Venta v : ventas) {
            if (esAnulada(v)) {
                continue;
            }
            String metodo = v.getMetodo_pago() != null ? v.getMetodo_pago().name() : "EFECTIVO";
            porMetodo.merge(metodo, 1, Integer::sum);
            montoPorMetodo.merge(metodo, v.getTotal_pago(), BigDecimal::add);

            String comprobante = v.getTipo_comprobante() != null ? v.getTipo_comprobante().name() : "BOLETA";
            porComprobante.merge(comprobante, 1, Integer::sum);
        }

        List<Map<String, Object>> metodos = new ArrayList<>();
        porMetodo.forEach((nombre, cantidad) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("nombre", nombre);
            item.put("ventas", cantidad);
            item.put("monto", montoPorMetodo.get(nombre).setScale(2, RoundingMode.HALF_UP));
            metodos.add(item);
        });

        List<Map<String, Object>> comprobantes = new ArrayList<>();
        porComprobante.forEach((nombre, cantidad) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("nombre", nombre);
            item.put("ventas", cantidad);
            comprobantes.add(item);
        });

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("metodosPago", metodos);
        resultado.put("comprobantes", comprobantes);
        return resultado;
    }

    private boolean esAnulada(Venta venta) {
        return venta.getEstadoVenta() != null && "ANULADA".equals(venta.getEstadoVenta().getNombre());
    }

    public Map<String, Object> getResumen() {
        List<Venta> ventas = ventaRepository.findAll();

        List<Venta> validas = ventas.stream().filter(v -> !esAnulada(v)).collect(java.util.stream.Collectors.toList());

        int totalVentas = validas.size();
        BigDecimal totalIngresos = validas.stream()
                .map(Venta::getTotal_pago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ticketPromedio = totalVentas > 0
                ? totalIngresos.divide(BigDecimal.valueOf(totalVentas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long vendedoresActivos = validas.stream()
                .map(v -> v.getUsuario().getId_usuario())
                .distinct()
                .count();

        LocalDate hoy = LocalDate.now();
        List<Venta> ventasDeHoy = validas.stream()
                .filter(v -> v.getFecha_venta().toLocalDate().equals(hoy))
                .collect(java.util.stream.Collectors.toList());
        BigDecimal ingresosHoy = ventasDeHoy.stream()
                .map(Venta::getTotal_pago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("totalVentas", totalVentas);
        resumen.put("totalIngresos", totalIngresos);
        resumen.put("ticketPromedio", ticketPromedio);
        resumen.put("vendedoresActivos", (int) vendedoresActivos);
        resumen.put("ventasHoy", ventasDeHoy.size());
        resumen.put("ingresosHoy", ingresosHoy.setScale(2, RoundingMode.HALF_UP));

        return resumen;
    }
}