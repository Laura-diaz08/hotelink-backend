package com.example.demo.hotelink.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.hotelink.model.CargoReserva;
import com.example.demo.hotelink.model.Cita;
import com.example.demo.hotelink.model.Factura;
import com.example.demo.hotelink.model.Reserva;
import com.example.demo.hotelink.repository.FacturaRepository;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class FacturaService {

    @Autowired
    private FacturaRepository repo;

    @Autowired
    private com.example.demo.hotelink.repository.CargoReservaRepository cargoReservaRepository;

    @Autowired
    private com.example.demo.hotelink.repository.CitaRepository citaRepository;

    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(repo.findAll());
    }

    public ResponseEntity<?> save(Factura f) {
        f.setFecha(LocalDate.now());
        f.setEstado("PENDIENTE");
        return ResponseEntity.ok(repo.save(f));
    }

    public byte[] generarPDF(Long reservaId) throws Exception {
        Factura factura = repo.findByReservaId(reservaId);
        if (factura == null) throw new RuntimeException("Factura no encontrada");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, baos); // ← línea crítica que faltaba
        document.open();

        // COLORES HOTELINK
        BaseColor orange = new BaseColor(255, 132, 87);
        BaseColor pink = new BaseColor(255, 95, 162);
        BaseColor dark = new BaseColor(31, 41, 55);
        BaseColor gris = new BaseColor(95, 100, 112);
        BaseColor cream = new BaseColor(255, 248, 242);
        BaseColor white = new BaseColor(255, 255, 255);
        BaseColor mintBg = new BaseColor(232, 248, 248);
        BaseColor borderColor = new BaseColor(229, 231, 235);

        // FUENTES
        Font fontLogo = new Font(Font.FontFamily.HELVETICA, 36, Font.BOLD, orange);
        Font fontLogoLink = new Font(Font.FontFamily.HELVETICA, 36, Font.BOLD, pink);
        Font fontSubtitulo = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, gris);
        Font fontNormal = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, dark);
        Font fontNegrita = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, dark);
        Font fontSeccion = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, orange);
        Font fontTotal = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, orange);
        Font fontBlanco = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, white);
        Font fontPie = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, gris);
        Font fontInfoTitle = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, white);
        Font fontInfoNormal = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, white);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{3, 2});
        header.setSpacingAfter(20);

        // Celda logo
        Paragraph logoParrafo = new Paragraph();
        logoParrafo.add(new Chunk("Hote", fontLogo));
        logoParrafo.add(new Chunk("link", fontLogoLink));
        PdfPCell logoCell = new PdfPCell();
        logoCell.addElement(logoParrafo);
        logoCell.addElement(new Paragraph("Sistema de Gestión Hotelera · Cádiz, España", fontSubtitulo));
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPadding(15);
        logoCell.setBackgroundColor(cream);
        header.addCell(logoCell);

        // Celda info factura
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBackgroundColor(orange);
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPadding(15);
        infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        infoCell.addElement(new Paragraph("FACTURA", fontInfoTitle));
        infoCell.addElement(new Paragraph("N.º " + factura.getId(), fontInfoNormal));
        infoCell.addElement(new Paragraph("Fecha: " + factura.getFecha().format(formatter), fontInfoNormal));
        infoCell.addElement(new Paragraph("Estado: " + factura.getEstado(), fontInfoNormal));
        header.addCell(infoCell);
        document.add(header);


        PdfPTable datosTable = new PdfPTable(2);
        datosTable.setWidthPercentage(100);
        datosTable.setWidths(new float[]{1, 1});
        datosTable.setSpacingAfter(20);

        // Celda cliente
        PdfPCell clienteCell = new PdfPCell();
        clienteCell.setBorder(Rectangle.NO_BORDER);
        clienteCell.setBackgroundColor(cream);
        clienteCell.setPadding(12);
        Paragraph clienteTitulo = new Paragraph("DATOS DEL CLIENTE", fontSeccion);
        clienteTitulo.setSpacingAfter(6);
        clienteCell.addElement(clienteTitulo);
        if (factura.getUsuario() != null) {
            clienteCell.addElement(new Paragraph(factura.getUsuario().getNombre(), fontNegrita));
            clienteCell.addElement(new Paragraph(factura.getUsuario().getEmail(), fontNormal));
        }
        datosTable.addCell(clienteCell);

        // Celda estancia
        PdfPCell estanciaCell = new PdfPCell();
        estanciaCell.setBorder(Rectangle.NO_BORDER);
        estanciaCell.setBackgroundColor(mintBg);
        estanciaCell.setPadding(12);
        Paragraph estanciaTitulo = new Paragraph("DETALLES DE LA ESTANCIA", fontSeccion);
        estanciaTitulo.setSpacingAfter(6);
        estanciaCell.addElement(estanciaTitulo);
        if (factura.getReserva() != null) {
            Reserva reserva = factura.getReserva();
            estanciaCell.addElement(new Paragraph(
                "Habitación " + reserva.getHabitacion().getNumero() +
                " · " + reserva.getHabitacion().getTipo(), fontNegrita));
            estanciaCell.addElement(new Paragraph(
                "Entrada: " + reserva.getFechaEntrada().format(formatter), fontNormal));
            estanciaCell.addElement(new Paragraph(
                "Salida: " + reserva.getFechaSalida().format(formatter), fontNormal));
            long noches = java.time.temporal.ChronoUnit.DAYS.between(
                reserva.getFechaEntrada(), reserva.getFechaSalida());
            estanciaCell.addElement(new Paragraph(noches + " noches", fontNormal));
        }

        datosTable.addCell(estanciaCell);
        document.add(datosTable);

        Paragraph desgloseTitle = new Paragraph("DESGLOSE DE CARGOS", fontSeccion);
        desgloseTitle.setSpacingAfter(8);
        document.add(desgloseTitle);

        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(5);
        tabla.setSpacingAfter(15);
        tabla.setWidths(new float[]{5, 2, 2});

        // Cabecera tabla
        for (String cab : new String[]{"CONCEPTO", "DETALLE", "IMPORTE"}) {
            PdfPCell cell = new PdfPCell(new Phrase(cab, fontBlanco));
            cell.setBackgroundColor(dark);
            cell.setPadding(10);
            cell.setBorder(Rectangle.NO_BORDER);
            tabla.addCell(cell);
        }

        if (factura.getReserva() != null) {
            Reserva reserva = factura.getReserva();
            long noches = java.time.temporal.ChronoUnit.DAYS.between(
                reserva.getFechaEntrada(), reserva.getFechaSalida());
            double precioHabitacion = reserva.getHabitacion().getPrecio();
            double subtotalHab = noches * precioHabitacion;

            addFilaTablaEstilo(tabla,
                "Habitación " + reserva.getHabitacion().getNumero() +
                " (" + reserva.getHabitacion().getTipo() + ")",
                noches + " noches x " + precioHabitacion + "€",
                subtotalHab + "€",
                fontNormal, cream);

            LocalDateTime inicio = reserva.getFechaEntrada().atStartOfDay();
            LocalDateTime fin = reserva.getFechaSalida().atTime(23, 59, 59);
            List<Cita> citas = citaRepository.findByUsuarioIdAndFechaHoraCitaBetween(
                reserva.getUsuario().getId(), inicio, fin);

            boolean alterno = true;
            for (Cita cita : citas) {
                if (!"CANCELADA".equals(cita.getEstado()) && cita.getServicio() != null) {
                    double precio = cita.getServicio().getPrecio() != null ?
                        cita.getServicio().getPrecio() : 0.0;
                    addFilaTablaEstilo(tabla,
                        "Servicio: " + cita.getServicio().getNombre(),
                        cita.getFechaHoraCita().format(
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")),
                        precio + "€",
                        fontNormal, alterno ? cream : white);
                    alterno = !alterno;
                }
            }

            List<CargoReserva> cargos = cargoReservaRepository.findByReservaId(reserva.getId());
            for (CargoReserva cargo : cargos) {
                if (cargo.getArticulo() != null) {
                    double subtotal = cargo.getCantidad() * cargo.getPrecioUnitario();
                    addFilaTablaEstilo(tabla,
                        "Artículo: " + cargo.getArticulo().getNombre(),
                        cargo.getCantidad() + " x " + cargo.getPrecioUnitario() + "€",
                        subtotal + "€",
                        fontNormal, alterno ? cream : white);
                    alterno = !alterno;
                }
            }
        }
        document.add(tabla);

        // =============================================
        // TOTAL
        // =============================================
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(50);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.setWidths(new float[]{1, 1});
        totalTable.setSpacingBefore(10);

        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL A PAGAR", fontBlanco));
        totalLabel.setBackgroundColor(orange);
        totalLabel.setBorder(Rectangle.NO_BORDER);
        totalLabel.setPadding(12);
        totalTable.addCell(totalLabel);

        PdfPCell totalValor = new PdfPCell(new Phrase(factura.getTotal() + " €", fontTotal));
        totalValor.setBackgroundColor(cream);
        totalValor.setBorder(Rectangle.NO_BORDER);
        totalValor.setPadding(12);
        totalValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.addCell(totalValor);
        document.add(totalTable);

        // =============================================
        // PIE DE PÁGINA
        // =============================================
        Paragraph pie = new Paragraph(
            "\n\nGracias por elegir Hotelink · Calle Ancha, 12 · Cádiz, España · info@hotelink.com",
            fontPie);
        pie.setAlignment(Element.ALIGN_CENTER);
        document.add(pie);

        document.close();
        return baos.toByteArray();
    }

    private void addFilaTablaEstilo(PdfPTable tabla, String concepto, String detalle,
                                    String importe, Font font, BaseColor bg) {
        PdfPCell c1 = new PdfPCell(new Phrase(concepto, font));
        PdfPCell c2 = new PdfPCell(new Phrase(detalle, font));
        PdfPCell c3 = new PdfPCell(new Phrase(importe, font));
        for (PdfPCell c : new PdfPCell[]{c1, c2, c3}) {
            c.setPadding(8);
            c.setBorder(Rectangle.BOTTOM);
            c.setBorderColor(new BaseColor(229, 231, 235));
            c.setBackgroundColor(bg);
        }
        tabla.addCell(c1);
        tabla.addCell(c2);
        tabla.addCell(c3);
    }
}