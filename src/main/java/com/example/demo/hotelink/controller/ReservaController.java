package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.auth.JwtService;
import com.example.demo.hotelink.model.Factura;
import com.example.demo.hotelink.model.Reserva;
import com.example.demo.hotelink.repository.ReservaRepository;
import com.example.demo.hotelink.service.ReservaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/reservas")
@CrossOrigin(origins = "*")
public class ReservaController {

    @Autowired
    private ReservaService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ReservaRepository repository;

    //Obtener todas las reservas
    @GetMapping
    public ResponseEntity<?> findAll(@RequestHeader(name="Authorization", required=false) String auth) {

        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN puede ver todas las reservas"));

        return service.findAll();
    }

    //Buscar reserva por id
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@RequestHeader(name="Authorization", required=false) String auth,
                                      @PathVariable Long id) {

        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        return service.findById(id);
    }

    //Crear una reserva
    @PostMapping
    public ResponseEntity<?> save(@RequestHeader(name="Authorization", required=false) String auth,
                                  @RequestBody Reserva r) {

        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        //Impedir que un usuario reserve en nombre de otro
        String usuarioToken = jwtService.obtenerNombre(auth.substring(7));

        //Si en la reserva viene un usuario asignado y su nombre no coincide con el del token
        if (r.getUsuario() != null && !r.getUsuario().getNombre().equals(usuarioToken)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "No puedes reservar con el nombre de otro usuario"));
        }

        return service.save(r);
    }

    //Eliminar una reseña
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader(name="Authorization", required=false) String auth,
                                    @PathVariable Long id) {

        //Solo puede eliminarla si es ADMIN
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        return service.deleteById(id);
    }

    @PatchMapping("/{id}/checkin")
    public ResponseEntity<?> checkIn(@RequestHeader(name="Authorization", required=false) String auth,
                                    @PathVariable Long id) {

        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error", "Solo ADMIN"));

        return service.checkIn(id);
    }

    // Obtener las reservas de un usuario específico
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> findByUsuarioId(@RequestHeader(name="Authorization", required=false) String auth,
                                             @PathVariable Long usuarioId) {

        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        // (Opcional) Aquí podrías añadir una validación extra para que un usuario
        // solo pueda ver las suyas propias, pero de momento con que esté logueado nos vale.

        // Llamamos al servicio para que nos dé las reservas de ese usuario
        return ResponseEntity.ok(service.findByUsuarioId(usuarioId));
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<?> hacerCheckOut(@PathVariable Long id) {
        
        try {
            Factura facturaGenerada = service.realizarCheckOut(id);
            
            // SOLUCIÓN AL BUCLE: Creamos un pequeño "paquete" solo con el texto y el total
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Check-out realizado con éxito");
            respuesta.put("total", facturaGenerada.getTotal()); // Angular leerá este 'total'
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            e.printStackTrace(); // Esto forzará a que el error rojo salga en la consola si hay otro problema
            return ResponseEntity.badRequest().body("Error al hacer check-out: " + e.getMessage());
        }
    }

    @GetMapping("/cliente/{id}")
    public ResponseEntity<?> obtenerCitasPorCliente(
            @RequestHeader(name="Authorization", required=false) String auth, 
            @PathVariable Long id) {
        
        // Comprobamos el token
        if (!jwtService.usuarioValido(auth)) {
            return ResponseEntity.status(401).body("Token inválido");
        }
        
        return ResponseEntity.ok(repository.findByUsuarioId(id)); // (o findByUsuarioId)
    }
}
