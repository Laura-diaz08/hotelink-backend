package com.example.demo.hotelink.service;

import com.example.demo.hotelink.model.Incidencia;
import com.example.demo.hotelink.repository.IncidenciaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IncidenciaService {

    @Autowired
    private IncidenciaRepository repo;

    //Obtener todas las incidencias
    public ResponseEntity<?> findAll() {
        List<Incidencia> lista = repo.findAll();
        if (lista.isEmpty()) {
            return ResponseEntity.status(404).body("No hay incidencias registradas");
        }
        return ResponseEntity.ok(lista);
    }

    //Buscar incidencia por id
    public ResponseEntity<?> findById(Long id) {
        Incidencia i = repo.findById(id).orElse(null);
        if (i == null) {
            return ResponseEntity.status(404).body("Incidencia no encontrada");
        }
        return ResponseEntity.ok(i);
    }

    //Guardar una nueva incidencia
    public ResponseEntity<?> save(Incidencia i) {
        //Asignar fecha de creación automáticamente
        i.setFechaCreacion(LocalDateTime.now());

        //Estado por defecto si no se envía
        if (i.getEstado() == null || i.getEstado().isBlank()) {
            i.setEstado("ABIERTA");
        }

        return ResponseEntity.ok(repo.save(i));
    }

    //Cambiar estado de una incidencia existente
    public ResponseEntity<?> cambiarEstado(Long id, String nuevo) {
        Incidencia inc = repo.findById(id).orElse(null);

        if (inc == null) {
            return ResponseEntity.status(404).body("Incidencia no encontrada");
        }

        inc.setEstado(nuevo);
        repo.save(inc);

        return ResponseEntity.ok(inc);
    }

    //Eliminar incidencia por id
    public ResponseEntity<?> deleteById(Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.status(404).body("Incidencia no encontrada");
        }

        repo.deleteById(id);
        return ResponseEntity.ok("Incidencia eliminada correctamente");
    }
}
