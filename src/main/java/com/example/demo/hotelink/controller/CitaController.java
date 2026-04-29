package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.auth.JwtService;
import com.example.demo.hotelink.model.Cita;
import com.example.demo.hotelink.repository.CitaRepository;
import com.example.demo.hotelink.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/citas")
public class CitaController {

    @Autowired
    private CitaService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CitaRepository repository;

    @PostMapping
    public ResponseEntity<?> crearCita(@RequestHeader(name="Authorization", required=false) String auth,
                                       @RequestBody Cita c) {
        
        if (!jwtService.usuarioValido(auth)) {
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido o sesión expirada"));
        }

        return service.save(c);
    }

    @GetMapping("/cliente/{id}")
    public ResponseEntity<?> obtenerCitasPorCliente(
            @RequestHeader(name="Authorization", required=false) String auth, @PathVariable Long id) {
        
        // Si tu token no es válido, dará error
        if (auth == null || !jwtService.usuarioValido(auth)) {
            return ResponseEntity.status(401).body("Token inválido o ausente");
        }
        
        return ResponseEntity.ok(repository.findByUsuarioId(id)); 
    }

    
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoCita(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String nuevoEstado = body.get("estado");

        service.cambiarEstado(id, nuevoEstado);
        
        return ResponseEntity.ok().body("Estado actualizado correctamente");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCita(
            @RequestHeader(name="Authorization", required=false) String auth,
            @PathVariable Long id) {

        // Validamos que el usuario esté logueado
        if (!jwtService.usuarioValido(auth)) {
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        }

        return service.eliminarCita(id);
    }
}