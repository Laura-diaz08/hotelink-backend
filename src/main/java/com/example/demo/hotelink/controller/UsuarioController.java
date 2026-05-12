package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.auth.JwtService;
import com.example.demo.hotelink.model.Usuario;
import com.example.demo.hotelink.repository.UsuarioRepository;
import com.example.demo.hotelink.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository repo;

    //Verifica si el token no pertenece a un ADMIN
    private boolean noEsAdmin(String auth) {
        return !jwtService.adminValido(auth);
    }

    //Obtener todos los usuarios
    @GetMapping
    public ResponseEntity<?> findAll(@RequestHeader(name="Authorization", required=false) String auth) {

        //Solo los ADMIN pueden ver la lista
        if (noEsAdmin(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN puede ver usuarios"));

        return service.findAll();
    }

    ///Obtener un usuario por id
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@RequestHeader(name="Authorization", required=false) String auth,
                                      @PathVariable Long id) {

        //Solo los ADMIN pueden consultar información especifica
        if (noEsAdmin(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        return service.findById(id);
    }

    //Crear usuario (el primero se permite sin token)
    @PostMapping
    public ResponseEntity<?> save(@RequestHeader(name="Authorization", required=false) String auth,
                                @RequestBody Usuario u) {

        if (service.noHayUsuarios()) {
            return service.save(u);
        }

        if (noEsAdmin(auth))
            return ResponseEntity.status(401).body(Map.of("error","Solo ADMIN puede crear usuarios"));

        return service.save(u);
    }

    //Eliminar un usuario por id
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader(name="Authorization", required=false) String auth,
                                    @PathVariable Long id) {

        //Solo puede eliminarlo si es ADMIN
        if (noEsAdmin(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        return service.deleteById(id);
    }
    

    @PutMapping("/{id}")
    public ResponseEntity<?> update(Long id, Usuario u) {
        Usuario existente = repo.findById(id).orElse(null);
        if (existente == null)
            return ResponseEntity.status(404).body("Usuario no encontrado");

        existente.setNombre(u.getNombre());
        existente.setEmail(u.getEmail());

        if (u.getPassword() != null && !u.getPassword().isBlank()) {
            existente.setPassword(passwordEncoder.encode(u.getPassword()));
        }

        try {
            return ResponseEntity.ok(repo.save(existente));
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error actualizando usuario: " + e.getMessage());
        }
    }
}
