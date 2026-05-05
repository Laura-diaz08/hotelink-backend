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
}