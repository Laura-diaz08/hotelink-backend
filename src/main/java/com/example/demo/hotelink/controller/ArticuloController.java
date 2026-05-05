package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.auth.JwtService;
import com.example.demo.hotelink.model.Articulo;
import com.example.demo.hotelink.service.ArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/articulos")
@CrossOrigin(origins = "*")
public class ArticuloController {

    @Autowired
    private ArticuloService service;

    @Autowired
    private JwtService jwtService;

    // Obtener artículos disponibles
    @GetMapping
    public ResponseEntity<List<Articulo>> getDisponibles(
            @RequestHeader(name="Authorization", required=false) String auth) {
        return ResponseEntity.ok(service.getArticulosDisponibles());
    }

    // Obtener todos (admin)
    @GetMapping("/todos")
    public ResponseEntity<?> getTodos(
            @RequestHeader(name="Authorization", required=false) String auth) {
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error", "Solo ADMIN"));
        return ResponseEntity.ok(service.getTodos());
    }

    // Añadir cargo a reserva
    @PostMapping("/cargo")
    public ResponseEntity<?> añadirCargo(
            @RequestHeader(name="Authorization", required=false) String auth,
            @RequestBody Map<String, Object> body) {
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error", "Solo ADMIN"));

        Long reservaId = Long.valueOf(body.get("reservaId").toString());
        Long articuloId = Long.valueOf(body.get("articuloId").toString());
        Integer cantidad = Integer.valueOf(body.get("cantidad").toString());

        return service.añadirCargo(reservaId, articuloId, cantidad);
    }

    // Obtener cargos de una reserva
    @GetMapping("/cargo/reserva/{reservaId}")
    public ResponseEntity<?> getCargos(
            @RequestHeader(name="Authorization", required=false) String auth,
            @PathVariable Long reservaId) {
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        return ResponseEntity.ok(service.getCargosDeReserva(reservaId));
    }

    // Eliminar cargo
    @DeleteMapping("/cargo/{id}")
    public ResponseEntity<?> eliminarCargo(
            @RequestHeader(name="Authorization", required=false) String auth,
            @PathVariable Long id) {
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error", "Solo ADMIN"));
        return service.eliminarCargo(id);
    }

    // Total de cargos de una reserva
    @GetMapping("/cargo/reserva/{reservaId}/total")
    public ResponseEntity<?> getTotalCargos(
            @RequestHeader(name="Authorization", required=false) String auth,
            @PathVariable Long reservaId) {
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        return ResponseEntity.ok(Map.of("total", service.getTotalCargos(reservaId)));
    }
}