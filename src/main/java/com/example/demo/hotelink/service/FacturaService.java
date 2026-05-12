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
import com.itextpdf.text.pdf.draw.LineSeparator;

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
        PdfWriter.getInstance(document, baos);
        document.open();

        BaseColor dorado = new BaseColor(201, 168, 76);
        BaseColor oscuro = new BaseColor(15, 15, 15);
        BaseColor gris = new BaseColor(107, 114, 128);

        Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 28, Font.BOLD, dorado);
        Font fontSubtitulo = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, gris);
        Font fontNormal = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, oscuro);
        Font fontNegrita = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, oscuro);
        Font fontTotal = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, dorado);

        Paragraph titulo = new Paragraph("HOTELINK", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("Calle Ancha, 12 · Cádiz, España · info@hotelink.com", fontSubtitulo);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(20);
        document.add(subtitulo);

        LineSeparator line = new LineSeparator(1, 100, dorado, Element.ALIGN_CENTER, -2);
        document.add(new Chunk(line));

        Paragraph facturaTitle = new Paragraph("\nFACTURA #" + factura.getId(), fontNegrita);
        facturaTitle.setSpacingBefore(15);
        document.add(facturaTitle);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        document.add(new Paragraph("Fecha: " + factura.getFecha().format(formatter), fontNormal));
        document.add(new Paragraph("Estado: " + factura.getEstado(), fontNormal));

        Paragraph clienteTitle = new Paragraph("\nDATOS DEL CLIENTE", fontNegrita);
        clienteTitle.setSpacingBefore(15);
        document.add(clienteTitle);

        if (factura.getUsuario() != null) {
            document.add(new Paragraph("Nombre: " + factura.getUsuario().getNombre(), fontNormal));
            document.add(new Paragraph("Email: " + factura.getUsuario().getEmail(), fontNormal));
        }

        Paragraph reservaTitle = new Paragraph("\nDETALLES DE LA ESTANCIA", fontNegrita);
        reservaTitle.setSpacingBefore(15);
        document.add(reservaTitle);

        if (factura.getReserva() != null) {
            Reserva reserva = factura.getReserva();
            document.add(new Paragraph("Habitación: " + reserva.getHabitacion().getNumero() +
                " (" + reserva.getHabitacion().getTipo() + ")", fontNormal));
            document.add(new Paragraph("Entrada: " + reserva.getFechaEntrada().format(formatter), fontNormal));
            document.add(new Paragraph("Salida: " + reserva.getFechaSalida().format(formatter), fontNormal));

            long noches = java.time.temporal.ChronoUnit.DAYS.between(
                reserva.getFechaEntrada(), reserva.getFechaSalida());
            double precioHabitacion = reserva.getHabitacion().getPrecio();
            document.add(new Paragraph("Noches: " + noches + " x " + precioHabitacion + "€ = " +
                (noches * precioHabitacion) + "€", fontNormal));
        }

        Paragraph desglose = new Paragraph("\nDESGLOSE", fontNegrita);
        desglose.setSpacingBefore(15);
        document.add(desglose);

        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10);
        tabla.setWidths(new float[]{5, 2, 2});

        PdfPCell[] headers = {
            new PdfPCell(new Phrase("Concepto", fontNegrita)),
            new PdfPCell(new Phrase("Cantidad", fontNegrita)),
            new PdfPCell(new Phrase("Importe", fontNegrita))
        };
        for (PdfPCell h : headers) {
            h.setBackgroundColor(dorado);
            h.setPadding(8);
            tabla.addCell(h);
        }

        if (factura.getReserva() != null) {
            Reserva reserva = factura.getReserva();
            long noches = java.time.temporal.ChronoUnit.DAYS.between(
                reserva.getFechaEntrada(), reserva.getFechaSalida());
            double precioHabitacion = reserva.getHabitacion().getPrecio();
            double subtotalHab = noches * precioHabitacion;

            addFilaTabla(tabla,
                "Habitación " + reserva.getHabitacion().getNumero() +
                " (" + reserva.getHabitacion().getTipo() + ")",
                noches + " noches x " + precioHabitacion + "€",
                subtotalHab + "€",
                fontNormal);

            LocalDateTime inicio = reserva.getFechaEntrada().atStartOfDay();
            LocalDateTime fin = reserva.getFechaSalida().atTime(23, 59, 59);

            List<Cita> citas = citaRepository
                .findByUsuarioIdAndFechaHoraCitaBetween(
                    reserva.getUsuario().getId(), inicio, fin);

            for (Cita cita : citas) {
                if (!"CANCELADA".equals(cita.getEstado()) && cita.getServicio() != null) {
                    double precio = cita.getServicio().getPrecio() != null ?
                        cita.getServicio().getPrecio() : 0.0;
                    addFilaTabla(tabla,
                        "Servicio: " + cita.getServicio().getNombre(),
                        cita.getFechaHoraCita().format(
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        precio + "€",
                        fontNormal);
                }
            }

            List<CargoReserva> cargos =
                cargoReservaRepository.findByReservaId(reserva.getId());

            for (CargoReserva cargo : cargos) {
                if (cargo.getArticulo() != null) {
                    double subtotal = cargo.getCantidad() * cargo.getPrecioUnitario();
                    addFilaTabla(tabla,
                        "Artículo: " + cargo.getArticulo().getNombre(),
                        cargo.getCantidad() + " x " + cargo.getPrecioUnitario() + "€",
                        subtotal + "€",
                        fontNormal);
                }
            }
        }

        document.add(tabla);

        document.add(new Chunk(new LineSeparator(1, 100, dorado, Element.ALIGN_CENTER, -2)));
        Paragraph total = new Paragraph("\nTOTAL: " + factura.getTotal() + "€", fontTotal);
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(10);
        document.add(total);

        Paragraph pie = new Paragraph(
            "\n\nGracias por elegir Hotelink. Esperamos volver a recibirle pronto.",
            fontSubtitulo);
        pie.setAlignment(Element.ALIGN_CENTER);
        document.add(pie);

        document.close();
        return baos.toByteArray();
    }

    private void addFilaTabla(PdfPTable tabla, String concepto, String cantidad, 
                           String importe, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(concepto, font));
        PdfPCell c2 = new PdfPCell(new Phrase(cantidad, font));
        PdfPCell c3 = new PdfPCell(new Phrase(importe, font));
        c1.setPadding(6);
        c2.setPadding(6);
        c3.setPadding(6);
        tabla.addCell(c1);
        tabla.addCell(c2);
        tabla.addCell(c3);
    }
}
