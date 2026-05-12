package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.auth.JwtService;
import com.example.demo.hotelink.model.Cita;
import com.example.demo.hotelink.model.Rol;
import com.example.demo.hotelink.repository.CitaRepository;
import com.example.demo.hotelink.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/citas")
public class CitaController {

    @Autowired
    private CitaService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CitaRepository repository;

    @PostMapping
    public ResponseEntity<?> crearCita(@RequestHeader(name="Authorization", required=false) String auth,
                                       @RequestBody Cita c) {
        
        if (!jwtService.usuarioValido(auth)) {
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido o sesión expirada"));
        }

        return service.save(c);
    }

    @GetMapping("/cliente/{id}")
    public ResponseEntity<?> obtenerCitasPorCliente(
            @RequestHeader(name="Authorization", required=false) String auth, @PathVariable Long id) {
        
        // Si tu token no es válido, dará error
        if (auth == null || !jwtService.usuarioValido(auth)) {
            return ResponseEntity.status(401).body("Token inválido o ausente");
        }
        
        return ResponseEntity.ok(repository.findByUsuarioId(id)); 
    }

    
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoCita(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String nuevoEstado = body.get("estado");

        service.cambiarEstado(id, nuevoEstado);
        
        return ResponseEntity.ok().body("Estado actualizado correctamente");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCita(
            @RequestHeader(name="Authorization", required=false) String auth,
            @PathVariable Long id) {

        // Validamos que el usuario esté logueado
        if (!jwtService.usuarioValido(auth)) {
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        }

        return service.eliminarCita(id);
    }

    @GetMapping("/usuario/{usuarioId}/entre-fechas")
    public ResponseEntity<?> getCitasEntresFechas(
            @RequestHeader(name="Authorization", required=false) String auth,
            @PathVariable Long usuarioId,
            @RequestParam String inicio,
            @RequestParam String fin) {

        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));

        LocalDateTime inicioFecha = LocalDateTime.parse(inicio + "T00:00:00");
        LocalDateTime finFecha = LocalDateTime.parse(fin + "T23:59:59");

        return ResponseEntity.ok(
            repository.findByUsuarioIdAndFechaHoraCitaBetween(usuarioId, inicioFecha, finFecha)
        );
    }

    @GetMapping("/todas")
    public ResponseEntity<?> todasLasCitas(
            @RequestHeader(name="Authorization", required=false) String auth) {

        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime finSemana = ahora.plusDays(7);

        return ResponseEntity.ok(
            repository.findByFechaHoraCitaBetween(ahora, finSemana)
        );
    }

    // Obtener citas sin empleado asignado
    @GetMapping("/disponibles")
    public ResponseEntity<?> getCitasDisponibles(
            @RequestHeader(name="Authorization", required=false) String auth) {
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        
        String token = auth.substring(7);
        Rol rol = jwtService.obtenerRol(token);
        
        return ResponseEntity.ok(repository.findCitasDisponiblesPorRol(rol.name()));
    }

    // Asignarse una cita
    @PutMapping("/{id}/asignar")
    public ResponseEntity<?> asignarEmpleado(
            @RequestHeader(name="Authorization", required=false) String auth,
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        return service.asignarEmpleado(id, body.get("empleadoId"));
    }

    // Obtener citas de un empleado
    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<?> getCitasEmpleado(
            @RequestHeader(name="Authorization", required=false) String auth,
            @PathVariable Long empleadoId) {
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        return ResponseEntity.ok(repository.findByEmpleadoId(empleadoId));
    }
    
}