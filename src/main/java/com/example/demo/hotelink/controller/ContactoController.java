package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/contacto")
@CrossOrigin(origins = "*")
public class ContactoController {

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<?> enviarMensaje(@RequestBody Map<String, String> body) {
        String nombre = body.get("nombre");
        String email = body.get("email");
        String mensaje = body.get("mensaje");

        if (nombre == null || nombre.isBlank() ||
            email == null || email.isBlank() ||
            mensaje == null || mensaje.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Todos los campos son obligatorios"));
        }

        try {
            emailService.enviarMensajeContacto(nombre, email, mensaje);
            return ResponseEntity.ok(Map.of("mensaje", "Mensaje enviado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al enviar el mensaje: " + e.getMessage()));
        }
    }
}