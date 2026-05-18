package com.example.demo.hotelink.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailRemitente;

    private String plantilla(String contenido) {
        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'></head>" +
            "<body style='margin:0;padding:0;background:#f9f9f9;font-family:Arial,sans-serif;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f9f9f9;padding:30px 0;'>" +
            "<tr><td align='center'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='max-width:560px;background:#ffffff;border-radius:12px;overflow:hidden;'>" +

            // CABECERA
            "<tr><td style='padding:28px 40px;border-bottom:3px solid #ff8457;'>" +
            "<span style='font-size:26px;font-weight:900;color:#ff8457;'>Hote</span>" +
            "<span style='font-size:26px;font-weight:900;color:#ff5fa2;'>link</span>" +
            "<span style='font-size:12px;color:#9ca3af;margin-left:12px;'>· Cádiz, España</span>" +
            "</td></tr>" +

            // CONTENIDO
            "<tr><td style='padding:36px 40px;'>" + contenido + "</td></tr>" +

            // PIE
            "<tr><td style='padding:20px 40px;background:#fff8f2;border-top:1px solid #ffe4d0;text-align:center;'>" +
            "<p style='margin:0;color:#9ca3af;font-size:12px;'>© 2026 Hotelink · info@hotelink.com</p>" +
            "</td></tr>" +

            "</table></td></tr></table></body></html>";
    }

    private String saludo(String nombre) {
        return "<p style='margin:0 0 20px;font-size:16px;color:#1f2937;'>Hola, <strong>" + nombre + "</strong></p>";
    }

    private String recuadro(String contenido) {
        return "<div style='background:#fff8f2;border-left:4px solid #ff8457;border-radius:8px;" +
            "padding:18px 20px;margin:20px 0;width:100%;box-sizing:border-box;'>" +
            contenido + "</div>";
    }

    private String fila(String etiqueta, String valor) {
        return "<p style='margin:6px 0;font-size:14px;color:#374151;'>" +
               "<strong style='color:#1f2937;'>" + etiqueta + ":</strong> " + valor + "</p>";
    }

    private String codigoBloque(String codigo) {
        return "<div style='text-align:center;margin:24px 0;'>" +
               "<span style='display:inline-block;background:#1f2937;color:#ff8457;font-size:32px;" +
               "font-weight:900;letter-spacing:10px;padding:16px 32px;border-radius:10px;'>" +
               codigo + "</span></div>";
    }

    private String despedida() {
        return "<p style='margin:24px 0 0;font-size:14px;color:#5f6470;'>El equipo de Hotelink</p>";
    }

    private void enviarHtml(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(emailRemitente);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Error enviando email: " + e.getMessage());
        }
    }


    public void enviarCodigoVerificacion(String email, String nombre, String codigo) {
        String contenido = saludo(nombre) +
            "<p style='color:#5f6470;font-size:14px;margin:0 0 8px;'>Gracias por registrarte en Hotelink. " +
            "Tu código de verificación es:</p>" +
            codigoBloque(codigo) +
            "<p style='color:#9ca3af;font-size:13px;text-align:center;'>El código caduca en 24 horas.</p>" +
            despedida();
        enviarHtml(email, "Verifica tu cuenta - Hotelink", plantilla(contenido));
    }

    public void enviarBienvenida(String email, String nombre) {
        String contenido = saludo(nombre) +
            "<p style='color:#5f6470;font-size:14px;margin:0 0 16px;'>¡Tu cuenta ha sido verificada correctamente! " +
            "Ya puedes disfrutar de todos los servicios de Hotelink.</p>" +
            recuadro(
                "<p style='margin:0 0 8px;font-size:14px;color:#1f2937;'><strong>Desde nuestra web puedes:</strong></p>" +
                "<p style='margin:4px 0;font-size:14px;color:#5f6470;'>✓ Reservar habitaciones</p>" +
                "<p style='margin:4px 0;font-size:14px;color:#5f6470;'>✓ Gestionar tus estancias</p>" +
                "<p style='margin:4px 0;font-size:14px;color:#5f6470;'>✓ Disfrutar de servicios exclusivos</p>"
            ) +
            despedida();
        enviarHtml(email, "¡Bienvenido a Hotelink, " + nombre + "!", plantilla(contenido));
    }

    public void enviarConfirmacionReserva(String email, String nombre, String habitacion,
                                           String tipo, String fechaEntrada, String fechaSalida, int huespedes) {
        String contenido = saludo(nombre) +
            "<p style='color:#5f6470;font-size:14px;margin:0 0 8px;'>¡Tu reserva ha sido confirmada!</p>" +
            recuadro(
                fila("Habitación", habitacion + " (" + tipo + ")") +
                fila("Entrada", fechaEntrada) +
                fila("Salida", fechaSalida) +
                fila("Huéspedes", String.valueOf(huespedes))
            ) +
            "<p style='color:#5f6470;font-size:14px;'>Puedes gestionar tu reserva desde tu panel en la web.</p>" +
            despedida();
        enviarHtml(email, "Reserva confirmada - Hotelink", plantilla(contenido));
    }

    public void enviarConfirmacionServicio(String email, String nombre, String servicio,
                                            String fecha, String hora, double precio) {
        String contenido = saludo(nombre) +
            "<p style='color:#5f6470;font-size:14px;margin:0 0 8px;'>¡Tu cita ha sido confirmada!</p>" +
            recuadro(
                fila("Servicio", servicio) +
                fila("Fecha", fecha) +
                fila("Hora", hora) +
                fila("Precio", precio + "€")
            ) +
            despedida();
        enviarHtml(email, "Cita confirmada - Hotelink", plantilla(contenido));
    }

    public void enviarAnulacionServicio(String email, String nombre, String servicio, String fecha) {
        String contenido = saludo(nombre) +
            "<p style='color:#5f6470;font-size:14px;margin:0 0 8px;'>Tu cita ha sido anulada correctamente.</p>" +
            recuadro(
                fila("Servicio", servicio) +
                fila("Fecha", fecha)
            ) +
            "<p style='color:#5f6470;font-size:14px;'>Si deseas reservar otra cita, puedes hacerlo desde nuestra web.</p>" +
            despedida();
        enviarHtml(email, "Cita anulada - Hotelink", plantilla(contenido));
    }

    public void enviarCancelacionReserva(String email, String nombre, String habitacion,
                                          String fechaEntrada, String fechaSalida) {
        String contenido = saludo(nombre) +
            "<p style='color:#5f6470;font-size:14px;margin:0 0 8px;'>Tu reserva ha sido cancelada correctamente.</p>" +
            recuadro(
                fila("Habitación", habitacion) +
                fila("Entrada", fechaEntrada) +
                fila("Salida", fechaSalida)
            ) +
            "<p style='color:#5f6470;font-size:14px;'>Si deseas hacer una nueva reserva, puedes hacerlo desde nuestra web.</p>" +
            despedida();
        enviarHtml(email, "Reserva cancelada - Hotelink", plantilla(contenido));
    }

    public void enviarCodigoRecuperacion(String email, String nombre, String codigo) {
        String contenido = saludo(nombre) +
            "<p style='color:#5f6470;font-size:14px;margin:0 0 8px;'>Has solicitado recuperar tu contraseña. " +
            "Tu código es:</p>" +
            codigoBloque(codigo) +
            "<p style='color:#9ca3af;font-size:13px;text-align:center;'>Si no has solicitado esto, ignora este mensaje.</p>" +
            despedida();
        enviarHtml(email, "Recuperación de contraseña - Hotelink", plantilla(contenido));
    }

    public void enviarMensajeContacto(String nombre, String email, String mensaje) {
        String contenido =
            "<p style='color:#5f6470;font-size:14px;margin:0 0 8px;'>Has recibido un nuevo mensaje de contacto:</p>" +
            recuadro(
                fila("Nombre", nombre) +
                fila("Email", email) +
                "<p style='margin:10px 0 4px;font-size:14px;color:#1f2937;'><strong>Mensaje:</strong></p>" +
                "<p style='margin:0;font-size:14px;color:#5f6470;'>" + mensaje + "</p>"
            );
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(emailRemitente);
            helper.setTo(emailRemitente);
            helper.setReplyTo(email);
            helper.setSubject("Nuevo mensaje de contacto - " + nombre);
            helper.setText(plantilla(contenido), true);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Error enviando email de contacto: " + e.getMessage());
        }
    }
}