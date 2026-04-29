package com.example.demo.hotelink.service;

import com.example.demo.hotelink.model.Cita;
import com.example.demo.hotelink.model.Servicio;
import com.example.demo.hotelink.repository.CitaRepository;
import com.example.demo.hotelink.repository.ServicioRepository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CitaService {

    @Autowired
    private CitaRepository repo;

   @Autowired
    private ServicioRepository servicioRepo; 

    public ResponseEntity<?> save(Cita c) {
        try {
            // 1. Buscamos el servicio original para saber su aforo máximo
            Servicio servicioBD = servicioRepo.findById(c.getServicio().getId())
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

            // 2. Contamos cuántas personas han reservado ya para ese día y hora exacta
            int reservasActuales = repo.countByServicioIdAndFechaHoraCita(
                    servicioBD.getId(), c.getFechaHoraCita()
            );

            // 3. Comprobamos si ya no cabe nadie más
            if (reservasActuales >= servicioBD.getAforoMaximo()) {
                // Devolvemos un error 400 con un mensaje claro
                return ResponseEntity.badRequest().body(Map.of("error", "Lo sentimos, el aforo está completo para esa hora. Por favor, elige otra."));
            }

            // 4. Si hay hueco, guardamos la cita normalmente
            return ResponseEntity.ok(repo.save(c));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al guardar la cita: " + e.getMessage()));
        }
    }

    public void cambiarEstado(Long id, String estado) {
        // Buscamos la cita por ID
        Cita cita = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        
        // Cambiamos el estado y guardamos
        cita.setEstado(estado);
        repo.save(cita);
    }

    public ResponseEntity<?> eliminarCita(Long id) {
        try {
            if (!repo.existsById(id)) {
                return ResponseEntity.status(404).body(Map.of("error", "La cita no existe"));
            }
            repo.deleteById(id);
            return ResponseEntity.ok(Map.of("mensaje", "Cita eliminada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al eliminar: " + e.getMessage()));
        }
    }
}