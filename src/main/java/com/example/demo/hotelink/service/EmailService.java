package com.example.demo.hotelink.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailRemitente;

    public void enviarMensajeContacto(String nombre, String email, String mensaje) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailRemitente);
        message.setTo(emailRemitente); // El hotel recibe el mensaje en su propio email
        message.setReplyTo(email); // Para poder responder al cliente directamente
        message.setSubject("Nuevo mensaje de contacto - " + nombre);
        message.setText(
            "Has recibido un nuevo mensaje de contacto:\n\n" +
            "Nombre: " + nombre + "\n" +
            "Email: " + email + "\n\n" +
            "Mensaje:\n" + mensaje
        );
        mailSender.send(message);
    }

    public void enviarCodigoVerificacion(String email, String nombre, String codigo) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Bienvenido a Hotelink - Verifica tu cuenta");
        msg.setText(
            "Hola " + nombre + ",\n\n" +
            "Gracias por registrarte en Hotelink. Tu código de verificación es:\n\n" +
            "► " + codigo + " ◄\n\n" +
            "Introduce este código en la página de verificación para activar tu cuenta.\n" +
            "El código caduca en 24 horas.\n\n" +
            "El equipo de Hotelink"
        );
        mailSender.send(msg);
    }

    public void enviarBienvenida(String email, String nombre) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("¡Bienvenido a Hotelink, " + nombre + "!");
        msg.setText(
            "Hola " + nombre + ",\n\n" +
            "¡Tu cuenta ha sido verificada correctamente! Ya puedes disfrutar de todos los servicios de Hotelink.\n\n" +
            "Desde nuestra web puedes:\n" +
            "  ✓ Reservar habitaciones\n" +
            "  ✓ Gestionar tus estancias\n" +
            "  ✓ Disfrutar de nuestros servicios exclusivos\n\n" +
            "¡Te esperamos en Hotelink!\n\n" +
            "El equipo de Hotelink\n" +
            "Cádiz, España"
        );
        mailSender.send(msg);
    }

    public void enviarConfirmacionReserva(String email, String nombre, String habitacion, String tipo, String fechaEntrada, String fechaSalida, int huespedes) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Confirmación de reserva - Hotelink");
        msg.setText(
            "Hola " + nombre + ",\n\n" +
            "¡Tu reserva ha sido confirmada! Aquí tienes los detalles:\n\n" +
            "  🏨 Habitación: " + habitacion + " (" + tipo + ")\n" +
            "  📅 Fecha de entrada: " + fechaEntrada + "\n" +
            "  📅 Fecha de salida: " + fechaSalida + "\n" +
            "  👥 Número de huéspedes: " + huespedes + "\n\n" +
            "Si necesitas modificar o cancelar tu reserva, puedes hacerlo desde tu panel en nuestra web.\n\n" +
            "¡Te esperamos en Hotelink!\n\n" +
            "El equipo de Hotelink\n" +
            "Cádiz, España"
        );
        mailSender.send(msg);
    }

    public void enviarConfirmacionServicio(String email, String nombre, String servicio, String fecha, String hora, double precio) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Confirmación de cita - Hotelink");
        msg.setText(
            "Hola " + nombre + ",\n\n" +
            "¡Tu cita ha sido confirmada! Aquí tienes los detalles:\n\n" +
            "  💆 Servicio: " + servicio + "\n" +
            "  📅 Fecha: " + fecha + "\n" +
            "  ⏰ Hora: " + hora + "\n" +
            "  💶 Precio: " + precio + "€\n\n" +
            "Puedes gestionar tu cita desde tu panel en nuestra web.\n\n" +
            "¡Te esperamos en Hotelink!\n\n" +
            "El equipo de Hotelink\n" +
            "Cádiz, España"
        );
        mailSender.send(msg);
    }

    public void enviarAnulacionServicio(String email, String nombre, String servicio, String fecha) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Cita anulada - Hotelink");
        msg.setText(
            "Hola " + nombre + ",\n\n" +
            "Tu cita ha sido anulada correctamente:\n\n" +
            "  💆 Servicio: " + servicio + "\n" +
            "  📅 Fecha: " + fecha + "\n\n" +
            "Si deseas reservar otra cita, puedes hacerlo desde nuestra web.\n\n" +
            "El equipo de Hotelink\n" +
            "Cádiz, España"
        );
        mailSender.send(msg);
    }

    public void enviarCancelacionReserva(String email, String nombre, String habitacion, String fechaEntrada, String fechaSalida) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Reserva cancelada - Hotelink");
        msg.setText(
            "Hola " + nombre + ",\n\n" +
            "Tu reserva ha sido cancelada correctamente:\n\n" +
            "  🏨 Habitación: " + habitacion + "\n" +
            "  📅 Fecha de entrada: " + fechaEntrada + "\n" +
            "  📅 Fecha de salida: " + fechaSalida + "\n\n" +
            "Si deseas hacer una nueva reserva, puedes hacerlo desde nuestra web.\n\n" +
            "El equipo de Hotelink\n" +
            "Cádiz, España"
        );
        mailSender.send(msg);
    }

    public void enviarCodigoRecuperacion(String email, String nombre, String codigo) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Recuperación de contraseña - Hotelink");
        msg.setText(
            "Hola " + nombre + ",\n\n" +
            "Has solicitado recuperar tu contraseña. Tu código es:\n\n" +
            "► " + codigo + " ◄\n\n" +
            "Introduce este código en la página de recuperación.\n" +
            "Si no has solicitado esto, ignora este mensaje.\n\n" +
            "El equipo de Hotelink"
        );
        mailSender.send(msg);
    }
}