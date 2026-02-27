package com.example.demo.hotelink.service;

import com.example.demo.hotelink.model.Servicio;
import com.example.demo.hotelink.repository.ServicioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioService {

    @Autowired
    private ServicioRepository repo;

    //Obtener todos los servicios
    public ResponseEntity<?> findAll() {
        List<Servicio> lista = repo.findAll();
        if (lista.isEmpty()) {
            return ResponseEntity.status(404).body("No hay servicios registrados");
        }
        return ResponseEntity.ok(lista);
    }

    //Buscar servicio por id
    public ResponseEntity<?> findById(Long id) {
        Servicio s = repo.findById(id).orElse(null);
        if (s == null) {
            return ResponseEntity.status(404).body("Servicio no encontrado");
        }
        return ResponseEntity.ok(s);
    }

    //Guardar o actualizar un servicio
    public ResponseEntity<?> save(Servicio s) {
        try {
            return ResponseEntity.ok(repo.save(s));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al guardar servicio: " + e.getMessage());
        }
    }

    //Eliminar servicio por id
    public ResponseEntity<?> deleteById(Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.status(404).body("Servicio no encontrado");
        }
        repo.deleteById(id);
        return ResponseEntity.ok("Servicio eliminado correctamente");
    }
}
