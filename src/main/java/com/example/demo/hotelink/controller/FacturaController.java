package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.auth.JwtService;
import com.example.demo.hotelink.model.Factura;
import com.example.demo.hotelink.service.FacturaService;

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
}

