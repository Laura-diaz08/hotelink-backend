package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.auth.JwtService;
import com.example.demo.hotelink.model.Incidencia;
import com.example.demo.hotelink.service.IncidenciaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/incidencias")
@CrossOrigin(origins = "*")
public class IncidenciaController {

    @Autowired
    private IncidenciaService service;

    @Autowired
    private JwtService jwtService;

    //Obtener todas las incidencias
    @GetMapping
    public ResponseEntity<?> findAll(@RequestHeader(name="Authorization", required=false) String auth) {

        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        return service.findAll();
    }

    //Crear una incidencia
    @PostMapping
    public ResponseEntity<?> save(@RequestHeader(name="Authorization", required=false) String auth,
                                  @RequestBody Incidencia i) {

        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        return service.save(i);
    }

    //Cambiar el estado de una incidencia
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@RequestHeader(name="Authorization", required=false) String auth,
                                           @PathVariable Long id,
                                           @RequestBody Map<String,String> body) {

        //Solo puede cambiarlo si es ADMIN
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        return service.cambiarEstado(id, body.get("estado"));
    }

    //Eliminar una incidencia
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader(name="Authorization", required=false) String auth,
                                    @PathVariable Long id) {

        //Solo puede eliminarlo si es ADMIN
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        return service.deleteById(id);
    }
}
