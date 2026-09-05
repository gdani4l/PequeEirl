package com.peque.peque_backend.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.peque.peque_backend.dtos.AnularVentaRequestDTO;
import com.peque.peque_backend.models.DetalleNotaCredito;
import com.peque.peque_backend.models.DetalleVenta;
import com.peque.peque_backend.models.EstadoVenta;
import com.peque.peque_backend.models.Movimientos;
import com.peque.peque_backend.models.NotaCredito;
import com.peque.peque_backend.models.Producto;
import com.peque.peque_backend.models.Usuario;
import com.peque.peque_backend.models.Venta;
import com.peque.peque_backend.repositories.DetalleNotaCreditoRepository;
import com.peque.peque_backend.repositories.DetalleVentaRepository;
import com.peque.peque_backend.repositories.EstadoVentaRepository;
import com.peque.peque_backend.repositories.MovimientosRepository;
import com.peque.peque_backend.repositories.NotaCreditoRepository;
import com.peque.peque_backend.repositories.ProductoRepository;
import com.peque.peque_backend.repositories.UsuarioRepository;
import com.peque.peque_backend.repositories.VentaRepository;

@Service
public class MovimientosService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private EstadoVentaRepository estadoVentaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotaCreditoRepository notaCreditoRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private DetalleNotaCreditoRepository detalleNotaCreditoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientosRepository movimientosRepository;

    @Autowired
    private PdfService pdfService;

    public List<Map<String, Object>> listarMovimientos() {
        List<Venta> ventas = ventaRepository.findAll();
        ventas.sort((a, b) -> b.getFecha_venta().compareTo(a.getFecha_venta()));
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Venta venta : ventas) {
            Map<String, Object> item = new HashMap<>();
            item.put("idMovimiento", venta.getId_venta());
            item.put("idVenta", venta.getId_venta());
            item.put("serieVenta", venta.getSerie());
            item.put("numeroVenta", venta.getNumero());
            item.put("totalVenta", venta.getTotal_pago());
            item.put("tipoComprobante",
                    venta.getTipo_comprobante() != null ? venta.getTipo_comprobante().name() : "BOLETA");
            item.put("estadoNuevo", venta.getEstadoVenta() != null ? venta.getEstadoVenta().getNombre() : "PENDIENTE");

            List<Movimientos> movimientos = movimientosRepository.findByVentaId(venta.getId_venta());
            Movimientos ultimo = movimientos.isEmpty() ? null : movimientos.get(0);

            if (ultimo != null) {
                item.put("estadoAnterior",
                        ultimo.getEstadoAnterior() != null ? ultimo.getEstadoAnterior().getNombre() : null);
                item.put("fechaMovimiento", ultimo.getFecha_movimiento());
                item.put("usuarioResponsable",
                        ultimo.getUsuario() != null
                                ? ultimo.getUsuario().getNombre() + " " + ultimo.getUsuario().getApellido_pat()
                                : "Usuario no disponible");
                item.put("observacion", ultimo.getObservacion());
            } else {
                item.put("estadoAnterior", null);
                item.put("fechaMovimiento", venta.getFecha_venta());
                item.put("usuarioResponsable",
                        venta.getUsuario() != null
                                ? venta.getUsuario().getNombre() + " " + venta.getUsuario().getApellido_pat()
                                : "Usuario no disponible");
                item.put("observacion", venta.getMotivo_anulacion());
            }

            Optional<NotaCredito> notaCredito = notaCreditoRepository.findByVentaOriginalId(venta.getId_venta());
            item.put("tieneNotaCredito", notaCredito.isPresent());
            item.put("idNotaCredito", notaCredito.isPresent() ? notaCredito.get().getId_nota_credito() : null);

            resultado.add(item);
        }

        return resultado;
    }

    @Transactional
    public Map<String, Object> anularVenta(Integer idVenta, AnularVentaRequestDTO request) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if ("ANULADA".equals(venta.getEstadoVenta().getNombre())) {
            throw new RuntimeException("La venta ya está anulada");
        }

        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        EstadoVenta estadoAnterior = venta.getEstadoVenta();
        EstadoVenta estadoAnulado = estadoVentaRepository.findByNombre("ANULADA")
                .orElseThrow(() -> new RuntimeException("Estado ANULADA no encontrado"));

        venta.setEstadoVenta(estadoAnulado);
        venta.setFecha_anulacion(LocalDateTime.now());
        venta.setMotivo_anulacion(request.getMotivo());
        ventaRepository.save(venta);

        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(idVenta);
        for (DetalleVenta detalle : detalles) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock().add(detalle.getCantidad()));
            productoRepository.save(producto);
        }

        Optional<Integer> maxNumero = notaCreditoRepository.findMaxNumeroBySerie("NC01");
        Integer nuevoNumero = maxNumero.orElse(0) + 1;

        NotaCredito notaCredito = new NotaCredito(
                "NC01",
                nuevoNumero,
                NotaCredito.TipoNota.TOTAL,
                venta.getTotal_pago(),
                request.getMotivo(),
                venta,
                usuario);
        notaCreditoRepository.save(notaCredito);

        for (DetalleVenta detalle : detalles) {
            DetalleNotaCredito detalleNota = new DetalleNotaCredito(
                    notaCredito,
                    detalle.getProducto(),
                    detalle.getCantidad(),
                    detalle.getPrecio_fijo());
            detalleNotaCreditoRepository.save(detalleNota);
        }

        Movimientos movimiento = new Movimientos(venta, estadoAnulado, usuario);
        movimiento.setEstadoAnterior(estadoAnterior);
        movimiento.setObservacion(request.getMotivo());
        movimientosRepository.save(movimiento);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("mensaje", "Venta anulada correctamente");
        resultado.put("idVenta", venta.getId_venta());
        resultado.put("idNotaCredito", notaCredito.getId_nota_credito());

        return resultado;
    }

    @Transactional
    public Map<String, Object> restaurarVenta(Integer idVenta, AnularVentaRequestDTO request) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if (!"ANULADA".equals(venta.getEstadoVenta().getNombre())) {
            throw new RuntimeException("La venta no está anulada");
        }

        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(idVenta);
        for (DetalleVenta detalle : detalles) {
            Producto producto = detalle.getProducto();
            if (producto.getStock().compareTo(detalle.getCantidad()) < 0) {
                throw new RuntimeException("No hay stock suficiente de " + producto.getNombre()
                        + " para restaurar la venta");
            }
        }

        for (DetalleVenta detalle : detalles) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock().subtract(detalle.getCantidad()));
            productoRepository.save(producto);
        }

        Optional<NotaCredito> notaCreditoOpt = notaCreditoRepository.findByVentaOriginalId(idVenta);
        if (notaCreditoOpt.isPresent()) {
            NotaCredito notaCredito = notaCreditoOpt.get();
            List<DetalleNotaCredito> detallesNota = detalleNotaCreditoRepository
                    .buscarPorNotaCredito(notaCredito.getId_nota_credito());
            detalleNotaCreditoRepository.deleteAll(detallesNota);
            notaCreditoRepository.delete(notaCredito);
        }

        EstadoVenta estadoAnterior = venta.getEstadoVenta();
        EstadoVenta estadoPagada = estadoVentaRepository.findByNombre("PAGADA")
                .orElseThrow(() -> new RuntimeException("Estado PAGADA no encontrado"));

        venta.setEstadoVenta(estadoPagada);
        venta.setFecha_anulacion(null);
        venta.setMotivo_anulacion(null);
        ventaRepository.save(venta);

        Movimientos movimiento = new Movimientos(venta, estadoPagada, usuario);
        movimiento.setEstadoAnterior(estadoAnterior);
        movimiento.setObservacion("Anulación deshecha por el administrador");
        movimientosRepository.save(movimiento);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("restaurada", true);
        resultado.put("idVenta", venta.getId_venta());
        return resultado;
    }

    public byte[] generarComprobantePDF(Integer idVenta) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(idVenta);
        return pdfService.generarComprobantePDF(venta, detalles);
    }

    public byte[] generarNotaCreditoPDF(Integer idNotaCredito) {
        NotaCredito nc = notaCreditoRepository.findById(idNotaCredito)
                .orElseThrow(() -> new RuntimeException("Nota de crédito no encontrada"));
        Venta ventaOriginal = nc.getVentaOriginal();
        List<DetalleNotaCredito> detallesNota = detalleNotaCreditoRepository.buscarPorNotaCredito(idNotaCredito);
        if (!detallesNota.isEmpty()) {
            return pdfService.generarNotaCreditoPDFDesdeDetalleNota(nc, ventaOriginal, detallesNota);
        }
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(ventaOriginal.getId_venta());
        return pdfService.generarNotaCreditoPDF(nc, ventaOriginal, detalles);
    }
}