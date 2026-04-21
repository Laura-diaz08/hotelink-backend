package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.model.ReservaServicio;
import com.example.demo.hotelink.service.ReservaServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reservas-servicios")
@CrossOrigin(origins = "*")
public class ReservaServicioController {

    @Autowired
    private ReservaServicioService service;

    @GetMapping
    public List<ReservaServicio> listarTodas() {
        return service.obtenerTodas();
    }

    @PostMapping
    public ReservaServicio crear(@RequestBody ReservaServicio reserva) {
        reserva.setEstado("PENDIENTE");
        return service.guardar(reserva);
    }
}