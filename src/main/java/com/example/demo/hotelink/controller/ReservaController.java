package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.auth.JwtService;
import com.example.demo.hotelink.model.Reserva;
import com.example.demo.hotelink.service.ReservaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reservas")
@CrossOrigin(origins = "*")
public class ReservaController {

    @Autowired
    private ReservaService service;

    @Autowired
    private JwtService jwtService;

    //Obtener todas las reservas
    @GetMapping
    public ResponseEntity<?> findAll(@RequestHeader(name="Authorization", required=false) String auth) {

        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

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


}
