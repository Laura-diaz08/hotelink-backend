package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.auth.JwtService;
import com.example.demo.hotelink.model.Opinion;
import com.example.demo.hotelink.service.OpinionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/opiniones")
@CrossOrigin(origins = "*")
public class OpinionController {

    @Autowired
    private OpinionService service;

    @Autowired
    private JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<Opinion>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<?> getEstadisticas() {
        return ResponseEntity.ok(service.getEstadisticas());
    }

    @GetMapping("/puede-opinar/{usuarioId}")
    public ResponseEntity<?> puedeOpinar(
            @RequestHeader(name="Authorization", required=false) String auth,
            @PathVariable Long usuarioId) {
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        return ResponseEntity.ok(Map.of("puede", service.usuarioPuedeOpinar(usuarioId)));
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestHeader(name="Authorization", required=false) String auth,
            @RequestBody Map<String, Object> body) {
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));

        Long usuarioId = Long.valueOf(body.get("usuarioId").toString());
        Integer estrellas = Integer.valueOf(body.get("estrellas").toString());
        String comentario = body.get("comentario").toString();

        return service.crearOpinion(usuarioId, estrellas, comentario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @RequestHeader(name="Authorization", required=false) String auth,
            @PathVariable Long id) {
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error", "Solo ADMIN"));
        return service.eliminar(id);
    }
}