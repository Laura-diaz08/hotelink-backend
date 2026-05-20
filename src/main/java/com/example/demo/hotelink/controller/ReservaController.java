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

import java.time.LocalDate;
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

        // El admin puede reservar en nombre de cualquier cliente
        if (!jwtService.adminValido(auth)) {
            String usuarioToken = jwtService.obtenerNombre(auth.substring(7));
            if (r.getUsuario() != null && !r.getUsuario().getNombre().equals(usuarioToken)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "No puedes reservar con el nombre de otro usuario"));
            }
        }

        return service.save(r);
    }

    //Eliminar una reserva de la base de datos 
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader(name="Authorization", required=false) String auth,
                                    @PathVariable Long id) {
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        return service.deleteById(id);
    }

    // --- NUEVOS MÉTODOS DE GESTIÓN ---

    // 1. Check-In 
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
    public ResponseEntity<?> hacerCheckOut(
            @RequestHeader(name="Authorization", required=false) String auth,
            @PathVariable Long id) {
        
        if (!jwtService.usuarioValido(auth)) {
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        }

        try {
            Factura facturaGenerada = service.realizarCheckOut(id);
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Check-out realizado con éxito");
            respuesta.put("total", facturaGenerada.getTotal());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 3. Cancelar Reserva 
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

    @PostMapping("/por-tipo")
    public ResponseEntity<?> reservarPorTipo(
            @RequestHeader(name="Authorization", required=false) String auth,
            @RequestBody Map<String, Object> body) {

        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));

        String tipo = (String) body.get("tipo");
        String entrada = (String) body.get("fechaEntrada");
        String salida = (String) body.get("fechaSalida");
        Long usuarioId = Long.valueOf(body.get("usuarioId").toString());
        int huespedes = Integer.parseInt(body.get("numeroHuespedes").toString());

        return service.reservarPorTipo(
            tipo,
            LocalDate.parse(entrada),
            LocalDate.parse(salida),
            usuarioId,
            huespedes
        );
    }
}