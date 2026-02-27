package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.auth.JwtService;
import com.example.demo.hotelink.model.Habitacion;
import com.example.demo.hotelink.service.HabitacionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/habitaciones")
@CrossOrigin(origins = "*")
public class HabitacionController {

    @Autowired
    private HabitacionService service;

    @Autowired
    private JwtService jwtService;

    //Obtener las habitaciones
    @GetMapping
    public ResponseEntity<?> findAll(@RequestHeader(name="Authorization", required=false) String auth) {

        //Verifica que el token sea válido
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        return service.findAll();
    }

    //Obtener una habitación por id
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@RequestHeader(name="Authorization", required=false) String auth,
                                      @PathVariable Long id) {

        //Verifica que el token sea válido
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        return service.findById(id);
    }

    //Crear una nueva habitación
    @PostMapping
    public ResponseEntity<?> save(@RequestHeader(name="Authorization", required=false) String auth,
                                  @RequestBody Habitacion h) {

        //Solo puede crearla si es ADMIN
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        return service.save(h);
    }

    //Cambiar el estado de una habitación
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@RequestHeader(name="Authorization", required=false) String auth,
                                           @PathVariable Long id,
                                           @RequestBody Map<String,String> body) {

        //Solo puede modificarla si es ADMIN
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        return service.cambiarEstado(id, body.get("estado"));
    }

    // Asignar un cliente a una habitación
    @PatchMapping("/{id}/asignar")
    public ResponseEntity<?> asignarCliente(@RequestHeader(name="Authorization", required=false) String auth,
                                            @PathVariable Long id,
                                            @RequestBody Map<String, Long> body) {

        // Solo puede asignar si es ADMIN
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN puede asignar habitaciones"));

        Long clienteId = body.get("clienteId");
        
        // Si mandan un clienteId nulo, asumimos que quieren liberar la habitación
        return service.asignarCliente(id, clienteId);
    }

    //Eliminar una habitación por id
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader(name="Authorization", required=false) String auth,
                                    @PathVariable Long id) {

        //Solo puede eliminarla si es ADMIN
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        return service.deleteById(id);
    }

    // Obtener las habitaciones de un cliente específico
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> findByCliente(@RequestHeader(name="Authorization", required=false) String auth,
                                           @PathVariable Long clienteId) {

        // Verifica que el token sea válido (cualquier usuario logueado puede ver sus habitaciones)
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        return service.obtenerHabitacionesDeCliente(clienteId);
    }

    // 1. BUSCAR HABITACIONES DISPONIBLES POR FECHAS
    // Ejemplo de uso: GET /habitaciones/disponibles?inicio=2024-05-01&fin=2024-05-07
    @GetMapping("/disponibles")
    public ResponseEntity<?> buscarDisponibles(
            @RequestHeader(name="Authorization", required=false) String auth,
            @RequestParam String inicio,
            @RequestParam String fin) {

        // Cualquier usuario logueado puede buscar disponibilidad
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));

        // El servicio se encargará de buscar qué habitaciones no se pisan con esas fechas
        return service.findDisponiblesPorFechas(inicio, fin);
    }

    // 2. HACER UNA RESERVA (Cualquier cliente logueado)
    @PostMapping("/{id}/reservar")
    public ResponseEntity<?> reservarHabitacion(
            @RequestHeader(name="Authorization", required=false) String auth,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        // Verifica que el usuario sea válido (un cliente normal puede reservar)
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión para reservar"));

        // Extraemos los datos que nos mandará Angular en el body
        Long clienteId = Long.valueOf(body.get("clienteId").toString());
        String fechaInicio = body.get("fechaInicio").toString();
        String fechaFin = body.get("fechaFin").toString();

        // El servicio comprobará si está libre y creará la reserva
        return service.crearReserva(id, clienteId, fechaInicio, fechaFin);
    }
}
