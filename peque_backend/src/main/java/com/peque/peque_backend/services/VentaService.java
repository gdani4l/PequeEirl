package com.peque.peque_backend.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.peque.peque_backend.dtos.DetalleVentaDTO;
import com.peque.peque_backend.dtos.VentaRequestDTO;
import com.peque.peque_backend.dtos.VentaResponseDTO;
import com.peque.peque_backend.models.Cliente;
import com.peque.peque_backend.models.DetalleVenta;
import com.peque.peque_backend.models.EstadoVenta;
import com.peque.peque_backend.models.Producto;
import com.peque.peque_backend.models.TipoDocumento;
import com.peque.peque_backend.models.Usuario;
import com.peque.peque_backend.models.Venta;
import com.peque.peque_backend.repositories.ClienteRepository;
import com.peque.peque_backend.repositories.DetalleVentaRepository;
import com.peque.peque_backend.repositories.EstadoVentaRepository;
import com.peque.peque_backend.repositories.ProductoRepository;
import com.peque.peque_backend.repositories.TipoDocumentoRepository;
import com.peque.peque_backend.repositories.UsuarioRepository;
import com.peque.peque_backend.repositories.VentaRepository;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EstadoVentaRepository estadoVentaRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private EmailService emailService;

    @Transactional
    private void aplicarNombreCliente(Cliente cliente, String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return;
        }
        String[] partes = nombreCompleto.trim().split("\\s+");
        if (partes.length >= 3) {
            cliente.setNombre(String.join(" ", java.util.Arrays.copyOfRange(partes, 0, partes.length - 2)));
            cliente.setApellido_pat(partes[partes.length - 2]);
            cliente.setApellido_mat(partes[partes.length - 1]);
        } else if (partes.length == 2) {
            cliente.setNombre(partes[0]);
            cliente.setApellido_pat(partes[1]);
        } else {
            cliente.setNombre(partes[0]);
        }
    }

    public BigDecimal calcularSubtotal(java.util.List<DetalleVentaDTO> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new RuntimeException("La venta debe tener al menos un producto");
        }
        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleVentaDTO detalle : detalles) {
            if (detalle.getIdProducto() == null) {
                throw new RuntimeException("Producto inválido en el detalle de la venta");
            }
            if (detalle.getCantidad() == null || detalle.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("La cantidad debe ser mayor a 0");
            }
            Producto producto = productoRepository.findById(detalle.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalle.getIdProducto()));
            subtotal = subtotal.add(producto.getPrecio_unitario().multiply(detalle.getCantidad()));
        }
        return subtotal.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTotalConIgv(java.util.List<DetalleVentaDTO> detalles) {
        BigDecimal subtotal = calcularSubtotal(detalles);
        BigDecimal igv = subtotal.multiply(new BigDecimal("0.18")).setScale(2, java.math.RoundingMode.HALF_UP);
        return subtotal.add(igv);
    }

    @Transactional
    public VentaResponseDTO registrarVenta(VentaRequestDTO request) {
        if (request.getClienteEmail() == null || request.getClienteEmail().trim().isEmpty()) {
            throw new RuntimeException("El correo electrónico del cliente es obligatorio");
        }
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        EstadoVenta estadoVenta = estadoVentaRepository.findByNombre("PAGADA")
                .orElseThrow(() -> new RuntimeException("Estado de venta no encontrado"));

        BigDecimal subtotal = calcularSubtotal(request.getDetalles());
        BigDecimal igv = subtotal.multiply(new BigDecimal("0.18")).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv);

        String serie = "B001";
        Integer numero = ventaRepository.findMaxNumeroBySerie(serie).orElse(0) + 1;

        Venta venta = new Venta();
        venta.setSerie(serie);
        venta.setNumero(numero);
        venta.setTotal_pago(total);
        venta.setMoneda("PEN");
        venta.setCondicion_pago(Venta.CondicionPago.CONTADO);
        venta.setMetodo_pago(
                "YAPE".equals(request.getMetodoPago()) ? Venta.MetodoPago.YAPE
                        : "TARJETA".equals(request.getMetodoPago()) ? Venta.MetodoPago.TARJETA
                                : Venta.MetodoPago.EFECTIVO);
        venta.setValor_venta(subtotal);
        venta.setIgv(igv);
        venta.setTasa_igv(new BigDecimal("18.00"));
        venta.setIsc(BigDecimal.ZERO);
        venta.setOtros_tributos(BigDecimal.ZERO);
        venta.setFecha_venta(LocalDateTime.now());
        venta.setUsuario(usuario);
        venta.setTipo_comprobante("FACTURA".equals(request.getTipoComprobante()) ? Venta.TipoComprobante.FACTURA
                : Venta.TipoComprobante.BOLETA);
        venta.setEstadoVenta(estadoVenta);

        if ("FACTURA".equals(request.getTipoComprobante()) && request.getClienteRUC() != null) {
            Optional<Cliente> clienteOpt = clienteRepository.findByNumeroDocumento(request.getClienteRUC());
            if (clienteOpt.isPresent()) {
                venta.setCliente(clienteOpt.get());
            } else {
                TipoDocumento tipoRUC = tipoDocumentoRepository.findByAbreviatura("RUC")
                        .orElseThrow(() -> new RuntimeException("Tipo documento RUC no encontrado"));

                Cliente nuevoCliente = new Cliente();
                nuevoCliente.setTipoDocumento(tipoRUC);
                nuevoCliente.setNumero_documento(request.getClienteRUC());
                nuevoCliente.setRazon_social(request.getClienteRazonSocial());
                nuevoCliente.setFecha_registro(LocalDateTime.now());
                Cliente clienteGuardado = clienteRepository.save(nuevoCliente);
                venta.setCliente(clienteGuardado);
            }
        } else if ("BOLETA".equals(request.getTipoComprobante()) && request.getClienteDNI() != null
                && !request.getClienteDNI().trim().isEmpty()) {
            Optional<Cliente> clienteOpt = clienteRepository.findByNumeroDocumento(request.getClienteDNI().trim());
            if (clienteOpt.isPresent()) {
                Cliente clienteExistente = clienteOpt.get();
                if (request.getClienteNombre() != null && !request.getClienteNombre().trim().isEmpty()) {
                    aplicarNombreCliente(clienteExistente, request.getClienteNombre());
                    clienteRepository.save(clienteExistente);
                }
                venta.setCliente(clienteExistente);
            } else {
                TipoDocumento tipoDNI = tipoDocumentoRepository.findByAbreviatura("DNI")
                        .orElseThrow(() -> new RuntimeException("Tipo documento DNI no encontrado"));

                Cliente nuevoCliente = new Cliente();
                nuevoCliente.setTipoDocumento(tipoDNI);
                nuevoCliente.setNumero_documento(request.getClienteDNI().trim());
                aplicarNombreCliente(nuevoCliente, request.getClienteNombre());
                nuevoCliente.setFecha_registro(LocalDateTime.now());
                Cliente clienteGuardado = clienteRepository.save(nuevoCliente);
                venta.setCliente(clienteGuardado);
            }
        }

        Venta ventaGuardada = ventaRepository.save(venta);

        java.util.List<DetalleVenta> detallesList = new java.util.ArrayList<>();
        for (DetalleVentaDTO detalleDTO : request.getDetalles()) {
            Producto producto = productoRepository.findById(detalleDTO.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalleDTO.getIdProducto()));

            BigDecimal stockActual = producto.getStock();
            BigDecimal nuevaCantidad = stockActual.subtract(detalleDTO.getCantidad());

            if (nuevaCantidad.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecio_fijo(producto.getPrecio_unitario());
            detalle.setVenta(ventaGuardada);
            detalle.setProducto(producto);
            DetalleVenta savedDetalle = detalleVentaRepository.save(detalle);
            detallesList.add(savedDetalle);

            producto.setStock(nuevaCantidad);
            productoRepository.save(producto);

            System.out.println("Producto: " + producto.getNombre() + " - Stock anterior: " + stockActual
                    + " - Vendido: " + detalleDTO.getCantidad() + " - Nuevo stock: " + nuevaCantidad);
        }

        if (request.getClienteEmail() != null && !request.getClienteEmail().trim().isEmpty()) {
            try {
                byte[] pdfBytes = pdfService.generarComprobantePDF(ventaGuardada, detallesList);
                
                String subject = "Su comprobante de pago " + ventaGuardada.getSerie() + "-" + String.format("%08d", ventaGuardada.getNumero());
                String text = "<html><body>" +
                        "<h2>¡Gracias por su compra!</h2>" +
                        "<p>Adjunto a este correo encontrará su comprobante de pago electrónico.</p>" +
                        "<p><b>Detalles de la compra:</b></p>" +
                        "<ul>" +
                        "<li><b>Comprobante:</b> " + ventaGuardada.getTipo_comprobante().name() + "</li>" +
                        "<li><b>Serie y Número:</b> " + ventaGuardada.getSerie() + "-" + String.format("%08d", ventaGuardada.getNumero()) + "</li>" +
                        "<li><b>Monto Total:</b> S/ " + ventaGuardada.getTotal_pago().setScale(2, java.math.RoundingMode.HALF_UP) + "</li>" +
                        "<li><b>Método de Pago:</b> " + ventaGuardada.getMetodo_pago().name() + "</li>" +
                        "</ul>" +
                        "<p>Saludos cordiales,<br>El equipo de Peque</p>" +
                        "</body></html>";
                
                String attachmentName = "Comprobante_" + ventaGuardada.getSerie() + "_" + String.format("%08d", ventaGuardada.getNumero()) + ".pdf";
                
                final String destinatario = request.getClienteEmail().trim();
                final String asuntoFinal = subject;
                final String cuerpoFinal = text;
                final byte[] pdfFinal = pdfBytes;
                final String adjuntoFinal = attachmentName;
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        emailService.sendEmailWithAttachment(destinatario, asuntoFinal, cuerpoFinal, pdfFinal, adjuntoFinal);
                    } catch (Exception ex) {
                        System.err.println("Error al enviar el comprobante por correo: " + ex.getMessage());
                    }
                });
            } catch (Exception ex) {
                System.err.println("Error al enviar el comprobante por correo: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        return new VentaResponseDTO(
                ventaGuardada.getId_venta(),
                ventaGuardada.getSerie(),
                ventaGuardada.getNumero(),
                ventaGuardada.getTotal_pago(),
                ventaGuardada.getFecha_venta(),
                "Venta registrada exitosamente");
    }
}