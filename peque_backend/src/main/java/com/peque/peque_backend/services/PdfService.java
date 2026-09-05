package com.peque.peque_backend.services;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.peque.peque_backend.models.DetalleNotaCredito;
import com.peque.peque_backend.models.DetalleVenta;
import com.peque.peque_backend.models.NotaCredito;
import com.peque.peque_backend.models.Venta;

@Service
public class PdfService {

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final String EMISOR_RAZON_SOCIAL = "PEQUE S.A.C.";
    private static final String EMISOR_RUC = "20511803994";
    private static final String EMISOR_DIRECCION = "AV. NICOLAS ARRIOLA 2446-2454 SAN LUIS - LIMA - PERU";

    private static final Color GRIS_LINEA = new Color(120, 120, 120);
    private static final Color GRIS_FONDO = new Color(235, 235, 235);

    public byte[] generarComprobantePDF(Venta venta, List<DetalleVenta> detalles) {
        boolean esFactura = "FACTURA".equals(venta.getTipo_comprobante().name());
        String titulo = esFactura ? "FACTURA ELECTRONICA" : "BOLETA DE VENTA ELECTRONICA";
        String numeroDoc = venta.getSerie() + "-" + String.format("%08d", venta.getNumero());

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float margin = 40;
                float pageWidth = page.getMediaBox().getWidth();
                float contentWidth = pageWidth - 2 * margin;
                float y = page.getMediaBox().getHeight() - margin;

                y = dibujarCabecera(cs, margin, y, contentWidth, titulo, numeroDoc);

                String nombreCliente;
                String docCliente;
                if (venta.getCliente() != null) {
                    if (esFactura) {
                        nombreCliente = venta.getCliente().getRazon_social() != null
                                ? venta.getCliente().getRazon_social()
                                : "-";
                        docCliente = "RUC: " + venta.getCliente().getNumero_documento();
                    } else {
                        String nom = venta.getCliente().getNombre() != null ? venta.getCliente().getNombre() : "";
                        String ape = venta.getCliente().getApellido_pat() != null
                                ? venta.getCliente().getApellido_pat()
                                : "";
                        nombreCliente = (nom + " " + ape).trim();
                        if (nombreCliente.isEmpty()) {
                            nombreCliente = "CLIENTE VARIOS";
                        }
                        docCliente = "DNI: " + venta.getCliente().getNumero_documento();
                    }
                } else {
                    nombreCliente = "CLIENTE VARIOS";
                    docCliente = "DNI: -";
                }

                String[][] datosCliente = {
                        { "Senor(es):", nombreCliente },
                        { "Documento:", docCliente },
                        { "Fecha de Emision:", venta.getFecha_venta().format(FMT_FECHA_HORA) },
                        { "Moneda:", "SOLES" },
                        { "Forma de pago:", "Contado - " + venta.getMetodo_pago().name() },
                        { "Vendedor:", venta.getUsuario().getNombre() + " " + venta.getUsuario().getApellido_pat() }
                };
                y = dibujarBloqueDatos(cs, margin, y, contentWidth, datosCliente);
                y -= 12;

                y = dibujarCabeceraTabla(cs, margin, y, contentWidth);

                BigDecimal factorSinIgv = new BigDecimal("1.18");
                for (DetalleVenta d : detalles) {
                    BigDecimal punit = d.getPrecio_fijo();
                    BigDecimal vunit = punit.divide(factorSinIgv, 2, RoundingMode.HALF_UP);
                    BigDecimal importe = punit.multiply(d.getCantidad()).setScale(2, RoundingMode.HALF_UP);
                    y = dibujarFilaTabla(cs, margin, y, contentWidth,
                            d.getCantidad().stripTrailingZeros().toPlainString(),
                            "NIU",
                            d.getProducto().getNombre(),
                            vunit.toPlainString(),
                            punit.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                            importe.toPlainString());
                }

                dibujarLineaHorizontal(cs, margin, y, contentWidth);
                y -= 18;

                BigDecimal opGravada = venta.getValor_venta().setScale(2, RoundingMode.HALF_UP);
                BigDecimal igv = venta.getIgv().setScale(2, RoundingMode.HALF_UP);
                BigDecimal total = venta.getTotal_pago().setScale(2, RoundingMode.HALF_UP);

                y = dibujarTotales(cs, margin, y, contentWidth, opGravada, igv, total);
                y -= 8;

                y = dibujarTexto(cs, FONT_BOLD, 9, margin, y,
                        "SON: " + montoEnLetras(total));
                y -= 12;

                String[] docClienteQR = datosDocClienteQR(venta, esFactura);
                String cadenaQR = construirCadenaQR(
                        esFactura ? "01" : "03",
                        venta.getSerie(), venta.getNumero(),
                        igv, total, venta.getFecha_venta(),
                        docClienteQR[0], docClienteQR[1]);

                float qrTamano = 90;
                float qrY = y - qrTamano;
                dibujarQR(doc, cs, cadenaQR, margin, qrY, qrTamano);

                dibujarLeyendas(cs, margin + qrTamano + 15, qrY + qrTamano - 12, new String[] {
                        "Representacion impresa de la " + titulo + ".",
                        "Autorizado mediante Resolucion de Intendencia N 034-005-0005315/SUNAT.",
                        "Consulte su comprobante en www.sunat.gob.pe (Opcion Consulta de Validez de CPE).",
                        "Resumen: " + calcularHash(cadenaQR).substring(0, 20)
                });
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF del comprobante: " + e.getMessage(), e);
        }
    }

    public byte[] generarNotaCreditoPDFDesdeDetalleNota(NotaCredito nc, Venta ventaOriginal,
            List<DetalleNotaCredito> detalles) {
        return generarNotaCreditoInterno(nc, ventaOriginal,
                detalles.stream().map(d -> new ItemComprobante(
                        d.getCantidad(), d.getProducto().getNombre(), d.getPrecio_unitario())).toList());
    }

    public byte[] generarNotaCreditoPDF(NotaCredito nc, Venta ventaOriginal, List<DetalleVenta> detalles) {
        return generarNotaCreditoInterno(nc, ventaOriginal,
                detalles.stream().map(d -> new ItemComprobante(
                        d.getCantidad(), d.getProducto().getNombre(), d.getPrecio_fijo())).toList());
    }

    private byte[] generarNotaCreditoInterno(NotaCredito nc, Venta ventaOriginal, List<ItemComprobante> items) {
        String numeroDoc = nc.getSerie() + "-" + String.format("%08d", nc.getNumero());
        boolean esFactura = "FACTURA".equals(ventaOriginal.getTipo_comprobante().name());
        String docModificado = (esFactura ? "FACTURA " : "BOLETA ")
                + ventaOriginal.getSerie() + "-" + String.format("%08d", ventaOriginal.getNumero());

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float margin = 40;
                float pageWidth = page.getMediaBox().getWidth();
                float contentWidth = pageWidth - 2 * margin;
                float y = page.getMediaBox().getHeight() - margin;

                y = dibujarCabecera(cs, margin, y, contentWidth, "NOTA DE CREDITO ELECTRONICA", numeroDoc);

                String nombreCliente = "CLIENTE VARIOS";
                String docCliente = "-";
                if (ventaOriginal.getCliente() != null) {
                    if (esFactura) {
                        nombreCliente = ventaOriginal.getCliente().getRazon_social() != null
                                ? ventaOriginal.getCliente().getRazon_social()
                                : "-";
                        docCliente = "RUC: " + ventaOriginal.getCliente().getNumero_documento();
                    } else {
                        String nom = ventaOriginal.getCliente().getNombre() != null
                                ? ventaOriginal.getCliente().getNombre()
                                : "";
                        String ape = ventaOriginal.getCliente().getApellido_pat() != null
                                ? ventaOriginal.getCliente().getApellido_pat()
                                : "";
                        String n = (nom + " " + ape).trim();
                        nombreCliente = n.isEmpty() ? "CLIENTE VARIOS" : n;
                        docCliente = "DNI: " + ventaOriginal.getCliente().getNumero_documento();
                    }
                }

                String[][] datos = {
                        { "Senor(es):", nombreCliente },
                        { "Documento:", docCliente },
                        { "Fecha de Emision:", nc.getFecha_emision().format(FMT_FECHA_HORA) },
                        { "Moneda:", "SOLES" },
                        { "Documento que modifica:", docModificado },
                        { "Fecha doc. modificado:", ventaOriginal.getFecha_venta().format(FMT_FECHA) },
                        { "Tipo de Nota de Credito:", "ANULACION DE LA OPERACION" },
                        { "Motivo o Sustento:", nc.getMotivo() != null ? nc.getMotivo() : "-" },
                        { "Emitida por:", nc.getUsuario().getNombre() + " " + nc.getUsuario().getApellido_pat() }
                };
                y = dibujarBloqueDatos(cs, margin, y, contentWidth, datos);
                y -= 12;

                y = dibujarCabeceraTabla(cs, margin, y, contentWidth);

                BigDecimal factorSinIgv = new BigDecimal("1.18");
                for (ItemComprobante item : items) {
                    BigDecimal vunit = item.precio.divide(factorSinIgv, 2, RoundingMode.HALF_UP);
                    BigDecimal importe = item.precio.multiply(item.cantidad).setScale(2, RoundingMode.HALF_UP);
                    y = dibujarFilaTabla(cs, margin, y, contentWidth,
                            item.cantidad.stripTrailingZeros().toPlainString(),
                            "NIU",
                            item.descripcion,
                            vunit.toPlainString(),
                            item.precio.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                            importe.toPlainString());
                }

                dibujarLineaHorizontal(cs, margin, y, contentWidth);
                y -= 18;

                BigDecimal total = nc.getMonto().setScale(2, RoundingMode.HALF_UP);
                BigDecimal opGravada = total.divide(factorSinIgv, 2, RoundingMode.HALF_UP);
                BigDecimal igv = total.subtract(opGravada);

                y = dibujarTotales(cs, margin, y, contentWidth, opGravada, igv, total);
                y -= 8;

                y = dibujarTexto(cs, FONT_BOLD, 9, margin, y, "SON: " + montoEnLetras(total));
                y -= 12;

                String[] docClienteQR = datosDocClienteQR(ventaOriginal, esFactura);
                String cadenaQR = construirCadenaQR(
                        "07",
                        nc.getSerie(), nc.getNumero(),
                        igv, total, nc.getFecha_emision(),
                        docClienteQR[0], docClienteQR[1]);

                float qrTamano = 90;
                float qrY = y - qrTamano;
                dibujarQR(doc, cs, cadenaQR, margin, qrY, qrTamano);

                dibujarLeyendas(cs, margin + qrTamano + 15, qrY + qrTamano - 12, new String[] {
                        "Representacion impresa de la NOTA DE CREDITO ELECTRONICA.",
                        "Documento emitido por la anulacion de la operacion indicada.",
                        "Consulte su comprobante en www.sunat.gob.pe (Opcion Consulta de Validez de CPE).",
                        "Resumen: " + calcularHash(cadenaQR).substring(0, 20)
                });
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de la nota de credito: " + e.getMessage(), e);
        }
    }

    private float dibujarCabecera(PDPageContentStream cs, float margin, float yTop, float contentWidth,
            String titulo, String numeroDoc) throws Exception {
        float y = yTop;

        dibujarTexto(cs, FONT_BOLD, 16, margin, y, EMISOR_RAZON_SOCIAL);
        y -= 16;
        dibujarTexto(cs, FONT, 9, margin, y, EMISOR_DIRECCION);
        y -= 12;
        dibujarTexto(cs, FONT, 9, margin, y, "Telefono: 946586414  -  correo: ventas@pequeperu.pe");

        float boxWidth = 200;
        float boxHeight = 70;
        float boxX = margin + contentWidth - boxWidth;
        float boxY = yTop - boxHeight + 12;

        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(1.2f);
        cs.addRect(boxX, boxY, boxWidth, boxHeight);
        cs.stroke();

        dibujarTextoCentrado(cs, FONT_BOLD, 11, boxX, boxY + boxHeight - 20, boxWidth, "R.U.C. N " + EMISOR_RUC);
        dibujarTextoCentrado(cs, FONT_BOLD, 10, boxX, boxY + boxHeight - 40, boxWidth, titulo);
        dibujarTextoCentrado(cs, FONT_BOLD, 11, boxX, boxY + boxHeight - 58, boxWidth, numeroDoc);

        return boxY - 20;
    }

    private float dibujarBloqueDatos(PDPageContentStream cs, float margin, float y, float contentWidth,
            String[][] datos) throws Exception {
        float alto = datos.length * 15 + 12;
        cs.setStrokingColor(GRIS_LINEA);
        cs.setLineWidth(0.8f);
        cs.addRect(margin, y - alto, contentWidth, alto);
        cs.stroke();

        float yLinea = y - 16;
        for (String[] fila : datos) {
            dibujarTexto(cs, FONT_BOLD, 9, margin + 8, yLinea, fila[0]);
            dibujarTexto(cs, FONT, 9, margin + 135, yLinea, recortar(fila[1], 80));
            yLinea -= 15;
        }
        return y - alto - 6;
    }

    private float dibujarCabeceraTabla(PDPageContentStream cs, float margin, float y, float contentWidth)
            throws Exception {
        float alto = 18;
        cs.setNonStrokingColor(GRIS_FONDO);
        cs.addRect(margin, y - alto + 4, contentWidth, alto);
        cs.fill();
        cs.setNonStrokingColor(Color.BLACK);

        dibujarTexto(cs, FONT_BOLD, 8, margin + 5, y - 9, "CANT.");
        dibujarTexto(cs, FONT_BOLD, 8, margin + 45, y - 9, "UM");
        dibujarTexto(cs, FONT_BOLD, 8, margin + 80, y - 9, "DESCRIPCION");
        dibujarTexto(cs, FONT_BOLD, 8, margin + contentWidth - 190, y - 9, "V. UNIT");
        dibujarTexto(cs, FONT_BOLD, 8, margin + contentWidth - 130, y - 9, "P. UNIT");
        dibujarTexto(cs, FONT_BOLD, 8, margin + contentWidth - 60, y - 9, "IMPORTE");

        dibujarLineaHorizontal(cs, margin, y - alto + 2, contentWidth);
        return y - alto - 8;
    }

    private float dibujarFilaTabla(PDPageContentStream cs, float margin, float y, float contentWidth,
            String cant, String um, String descripcion, String vunit, String punit, String importe)
            throws Exception {
        dibujarTexto(cs, FONT, 8, margin + 5, y, cant);
        dibujarTexto(cs, FONT, 8, margin + 45, y, um);
        dibujarTexto(cs, FONT, 8, margin + 80, y, recortar(descripcion, 48));
        dibujarTexto(cs, FONT, 8, margin + contentWidth - 190, y, vunit);
        dibujarTexto(cs, FONT, 8, margin + contentWidth - 130, y, punit);
        dibujarTexto(cs, FONT, 8, margin + contentWidth - 60, y, importe);
        return y - 14;
    }

    private float dibujarTotales(PDPageContentStream cs, float margin, float y, float contentWidth,
            BigDecimal opGravada, BigDecimal igv, BigDecimal total) throws Exception {
        float xLabel = margin + contentWidth - 190;
        float xValor = margin + contentWidth - 70;

        dibujarTexto(cs, FONT_BOLD, 9, xLabel, y, "OP. GRAVADA:");
        dibujarTexto(cs, FONT, 9, xValor, y, "S/ " + opGravada.toPlainString());
        y -= 15;
        dibujarTexto(cs, FONT_BOLD, 9, xLabel, y, "I.G.V. (18%):");
        dibujarTexto(cs, FONT, 9, xValor, y, "S/ " + igv.toPlainString());
        y -= 15;
        dibujarTexto(cs, FONT_BOLD, 10, xLabel, y, "IMPORTE TOTAL:");
        dibujarTexto(cs, FONT_BOLD, 10, xValor, y, "S/ " + total.toPlainString());
        return y - 15;
    }

    private void dibujarLeyendas(PDPageContentStream cs, float margin, float y, String[] leyendas) throws Exception {
        cs.setNonStrokingColor(new Color(90, 90, 90));
        for (String leyenda : leyendas) {
            dibujarTexto(cs, FONT, 7.5f, margin, y, leyenda);
            y -= 11;
        }
        cs.setNonStrokingColor(Color.BLACK);
    }

    private float dibujarTexto(PDPageContentStream cs, PDType1Font font, float size, float x, float y, String texto)
            throws Exception {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(limpiar(texto));
        cs.endText();
        return y - size - 4;
    }

    private void dibujarTextoCentrado(PDPageContentStream cs, PDType1Font font, float size, float x, float y,
            float ancho, String texto) throws Exception {
        String limpio = limpiar(texto);
        float anchoTexto = font.getStringWidth(limpio) / 1000 * size;
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x + (ancho - anchoTexto) / 2, y);
        cs.showText(limpio);
        cs.endText();
    }

    private void dibujarLineaHorizontal(PDPageContentStream cs, float x, float y, float ancho) throws Exception {
        cs.setStrokingColor(GRIS_LINEA);
        cs.setLineWidth(0.8f);
        cs.moveTo(x, y);
        cs.lineTo(x + ancho, y);
        cs.stroke();
    }

    private String recortar(String texto, int max) {
        if (texto == null) return "";
        return texto.length() > max ? texto.substring(0, max) + "..." : texto;
    }

    private String limpiar(String texto) {
        if (texto == null) return "";
        return texto
                .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                .replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U")
                .replace("ñ", "n").replace("Ñ", "N")
                .replaceAll("[^\\x20-\\x7E]", "");
    }

    private String montoEnLetras(BigDecimal monto) {
        long parteEntera = monto.longValue();
        int centimos = monto.remainder(BigDecimal.ONE).movePointRight(2).abs().intValue();
        String letras = numeroALetras(parteEntera).trim().toUpperCase();
        return letras + " CON " + String.format("%02d", centimos) + "/100 SOLES";
    }

    private String numeroALetras(long numero) {
        if (numero == 0) return "cero";
        if (numero < 0) return "menos " + numeroALetras(-numero);

        StringBuilder sb = new StringBuilder();

        if (numero >= 1_000_000) {
            long millones = numero / 1_000_000;
            sb.append(millones == 1 ? "un millon"
                    : numeroALetras(millones).replaceAll("uno$", "un") + " millones");
            numero %= 1_000_000;
            if (numero > 0) sb.append(" ");
        }

        if (numero >= 1000) {
            long miles = numero / 1000;
            sb.append(miles == 1 ? "mil" : numeroALetras(miles).replaceAll("uno$", "un") + " mil");
            numero %= 1000;
            if (numero > 0) sb.append(" ");
        }

        if (numero >= 100) {
            int centenas = (int) (numero / 100);
            if (numero == 100) {
                sb.append("cien");
            } else {
                String[] cent = { "", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos",
                        "seiscientos", "setecientos", "ochocientos", "novecientos" };
                sb.append(cent[centenas]);
            }
            numero %= 100;
            if (numero > 0) sb.append(" ");
        }

        if (numero >= 30) {
            String[] dec = { "", "", "", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta",
                    "noventa" };
            sb.append(dec[(int) (numero / 10)]);
            numero %= 10;
            if (numero > 0) sb.append(" y ");
        } else if (numero >= 20) {
            if (numero == 20) {
                sb.append("veinte");
                numero = 0;
            } else {
                sb.append("veinti");
            }
            numero %= 20;
        } else if (numero >= 10) {
            String[] especiales = { "diez", "once", "doce", "trece", "catorce", "quince", "dieciseis", "diecisiete",
                    "dieciocho", "diecinueve" };
            sb.append(especiales[(int) (numero - 10)]);
            numero = 0;
        }

        if (numero > 0) {
            String[] unidades = { "", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve" };
            sb.append(unidades[(int) numero]);
        }

        return sb.toString();
    }

    private String construirCadenaQR(String tipoComprobante, String serie, Integer numero,
            BigDecimal igv, BigDecimal total, java.time.LocalDateTime fechaEmision,
            String tipoDocCliente, String nroDocCliente) {
        String base = EMISOR_RUC + "|" + tipoComprobante + "|" + serie + "|"
                + String.format("%08d", numero) + "|"
                + igv.setScale(2, RoundingMode.HALF_UP).toPlainString() + "|"
                + total.setScale(2, RoundingMode.HALF_UP).toPlainString() + "|"
                + fechaEmision.format(FMT_FECHA) + "|"
                + tipoDocCliente + "|" + nroDocCliente;
        return base + "|" + calcularHash(base);
    }

    private String calcularHash(String contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenido.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash).substring(0, 28);
        } catch (Exception e) {
            return "";
        }
    }

    private byte[] generarImagenQR(String contenido, int pixeles) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, pixeles, pixeles, hints);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }

    private void dibujarQR(PDDocument doc, PDPageContentStream cs, String cadenaQR,
            float x, float y, float tamano) throws Exception {
        byte[] qrBytes = generarImagenQR(cadenaQR, 300);
        PDImageXObject imagen = PDImageXObject.createFromByteArray(doc, qrBytes, "qr");
        cs.drawImage(imagen, x, y, tamano, tamano);
    }

    private String[] datosDocClienteQR(Venta venta, boolean esFactura) {
        if (venta.getCliente() != null && venta.getCliente().getNumero_documento() != null) {
            return new String[] { esFactura ? "6" : "1", venta.getCliente().getNumero_documento() };
        }
        return new String[] { "-", "-" };
    }

    private static class ItemComprobante {
        final BigDecimal cantidad;
        final String descripcion;
        final BigDecimal precio;

        ItemComprobante(BigDecimal cantidad, String descripcion, BigDecimal precio) {
            this.cantidad = cantidad;
            this.descripcion = descripcion;
            this.precio = precio;
        }
    }
}
