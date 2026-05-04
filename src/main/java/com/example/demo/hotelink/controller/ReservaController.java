package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.auth.JwtService;
import com.example.demo.hotelink.dto.ReservaDTO;
import com.example.demo.hotelink.model.Factura;
import com.example.demo.hotelink.model.Reserva;
import com.example.demo.hotelink.repository.ReservaRepository;
import com.example.demo.hotelink.service.ReservaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reservas")
@CrossOrigin(origins = "*")
public class ReservaController {

    @Autowired
    private ReservaService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ReservaRepository repository;

    // Obtener todas las reservas
    @GetMapping
    public ResponseEntity<?> findAll(@RequestHeader(name="Authorization", required=false) String auth) {
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN puede ver todas las reservas"));

        List<ReservaDTO> dtos = service.findAll().stream()
                .map(service::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //Buscar reserva por id
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@RequestHeader(name="Authorization", required=false) String auth,
                                      @PathVariable Long id) {
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        return service.findById(id);
    }

    //Crear una reserva
    @PostMapping
    public ResponseEntity<?> save(@RequestHeader(name="Authorization", required=false) String auth,
                                  @RequestBody Reserva r) {
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        //Impedir que un usuario reserve en nombre de otro
        String usuarioToken = jwtService.obtenerNombre(auth.substring(7));

        if (r.getUsuario() != null && !r.getUsuario().getNombre().equals(usuarioToken)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "No puedes reservar con el nombre de otro usuario"));
        }

        return service.save(r);
    }

    //Eliminar una reserva de la base de datos (Físico)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader(name="Authorization", required=false) String auth,
                                    @PathVariable Long id) {
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        return service.deleteById(id);
    }

    // --- NUEVOS MÉTODOS DE GESTIÓN ---

    // 1. Check-In (¡Ojo! Cambiado a PutMapping para que coincida con Angular)
   @PutMapping("/{id}/checkin")
    public ResponseEntity<?> checkIn(@RequestHeader(name="Authorization", required=false) String auth,
                                     @PathVariable Long id) {

        if (!jwtService.usuarioValido(auth)) {
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión para hacer el check-in"));
        }

        return service.checkIn(id);
    }

    // 2. Check-Out
    @PostMapping("/{id}/checkout")
    public ResponseEntity<?> hacerCheckOut(@PathVariable Long id) {
        try {
            Factura facturaGenerada = service.realizarCheckOut(id);
            
            // Tu excelente solución al bucle:
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Check-out realizado con éxito");
            respuesta.put("total", facturaGenerada.getTotal()); 
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.badRequest().body(Map.of("error", "Error al hacer check-out: " + e.getMessage()));
        }
    }

    // 3. Cancelar Reserva (El método que nos faltaba)
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(@RequestHeader(name="Authorization", required=false) String auth,
                                             @PathVariable Long id) {
        if (!jwtService.usuarioValido(auth)) {
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        }
        return service.cancelarReserva(id);
    }

    // --- MÉTODOS DE BÚSQUEDA POR USUARIO ---

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> findByUsuarioId(@RequestHeader(name="Authorization", required=false) String auth,
                                             @PathVariable Long usuarioId) {
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        return ResponseEntity.ok(service.findByUsuarioId(usuarioId));
    }

    @GetMapping("/cliente/{id}")
    public ResponseEntity<?> obtenerCitasPorCliente(
            @RequestHeader(name="Authorization", required=false) String auth, 
            @PathVariable Long id) {
        if (!jwtService.usuarioValido(auth)) {
            return ResponseEntity.status(401).body("Token inválido");
        }
        return ResponseEntity.ok(repository.findByUsuarioId(id));
    }
}