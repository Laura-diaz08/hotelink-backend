package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.auth.JwtService;
import com.example.demo.hotelink.model.Servicio;
import com.example.demo.hotelink.service.ServicioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/servicios")
@CrossOrigin(origins = "*")
public class ServicioController {

    @Autowired
    private ServicioService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private com.example.demo.hotelink.repository.CitaRepository citaRepository;

    //Obtener todos los servicios
    @GetMapping
    public ResponseEntity<?> findAll(@RequestHeader(name="Authorization", required=false) String auth) {

        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        return service.findAll();
    }

    //Obtener un servicio por id
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@RequestHeader(name="Authorization", required=false) String auth,
                                      @PathVariable Long id) {

        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        return service.findById(id);
    }

    //Crear un nuevo servicio
    @PostMapping
    public ResponseEntity<?> save(@RequestHeader(name="Authorization", required=false) String auth,
                                  @RequestBody Servicio s) {

        //Solo puede crearlo si es ADMIN
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        return service.save(s);
    }

    //Eliminar un servicio por id
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader(name="Authorization", required=false) String auth,
                                    @PathVariable Long id) {

        //Solo puede eliminarlo si es ADMIN
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        return service.deleteById(id);
    }

    @GetMapping("/{id}/aforo")
    public ResponseEntity<?> getAforo(
            @RequestHeader(name="Authorization", required=false) String auth,
            @PathVariable Long id,
            @RequestParam String fecha,
            @RequestParam String hora) {

        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        Servicio servicio = (Servicio) service.findById(id).getBody();
        if (servicio == null)
            return ResponseEntity.status(404).body(Map.of("error","Servicio no encontrado"));

        LocalDateTime fechaHora = LocalDateTime.parse(fecha + "T" + hora);
        int citasActivas = citaRepository.countByServicioIdAndFechaHoraCita(id, fechaHora);
        int aforoDisponible = (servicio.getAforoMaximo() != null ? servicio.getAforoMaximo() : 0) - citasActivas;

        return ResponseEntity.ok(Map.of(
            "aforoMaximo", servicio.getAforoMaximo(),
            "citasActivas", citasActivas,
            "aforoDisponible", Math.max(0, aforoDisponible)
        ));
    }
}
