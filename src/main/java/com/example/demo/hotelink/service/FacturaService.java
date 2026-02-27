package com.example.demo.hotelink.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.hotelink.model.Factura;
import com.example.demo.hotelink.repository.FacturaRepository;

@Service
public class FacturaService {

    @Autowired
    private FacturaRepository repo;

    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(repo.findAll());
    }

    public ResponseEntity<?> save(Factura f) {
        f.setFecha(LocalDate.now());
        f.setEstado("PENDIENTE");
        return ResponseEntity.ok(repo.save(f));
    }
}
