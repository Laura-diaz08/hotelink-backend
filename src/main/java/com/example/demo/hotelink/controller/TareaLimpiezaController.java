package com.example.demo.hotelink.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.hotelink.model.TareaLimpieza;
import com.example.demo.hotelink.service.TareaLimpiezaService;

import java.util.List;

@RestController
@RequestMapping("/api/tareas-limpieza")
@CrossOrigin(origins = "*") // Imprescindible para Angular
public class TareaLimpiezaController {

    @Autowired
    private TareaLimpiezaService tareaService;

    // Angular llamará aquí para pintar la tabla
    @GetMapping
    public List<TareaLimpieza> listarTareas() {
        return tareaService.obtenerTodas();
    }
}
