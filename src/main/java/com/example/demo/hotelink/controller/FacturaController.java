package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.auth.JwtService;
import com.example.demo.hotelink.model.Factura;
import com.example.demo.hotelink.repository.FacturaRepository;
import com.example.demo.hotelink.service.FacturaService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/facturas")
@CrossOrigin("*")
public class FacturaController {

    @Autowired
    private FacturaService service;

    @Autowired
    private JwtService jwt;

    @Autowired
    private FacturaRepository repo;

    @GetMapping
    public ResponseEntity<?> findAll(@RequestHeader("Authorization") String auth) {
        if (!jwt.adminValido(auth))
            return ResponseEntity.status(403).body("Solo ADMIN");

        return service.findAll();
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestHeader("Authorization") String auth,
                                  @RequestBody Factura f) {
        if (!jwt.adminValido(auth))
            return ResponseEntity.status(403).body("Solo ADMIN");

        return service.save(f);
    }

    @GetMapping("/reserva/{reservaId}/pdf")
    public ResponseEntity<?> descargarFactura(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long reservaId) {

        if (!jwt.usuarioValido(auth))
            return ResponseEntity.status(401).body("Token inválido");

        try {
            byte[] pdf = service.generarPDF(reservaId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "factura-" + reservaId + ".pdf");
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al generar la factura: " + e.getMessage());
        }
    }

    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<?> getFacturaPorReserva(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long reservaId) {

        if (!jwt.usuarioValido(auth))
            return ResponseEntity.status(401).body("Token inválido");

        Factura factura = repo.findByReservaId(reservaId);
        if (factura == null)
            return ResponseEntity.status(404).body("Factura no encontrada");

        return ResponseEntity.ok(factura);
    }
}

