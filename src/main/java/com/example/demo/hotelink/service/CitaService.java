package com.example.demo.hotelink.service;

import com.example.demo.hotelink.model.Cita;
import com.example.demo.hotelink.model.Servicio;
import com.example.demo.hotelink.repository.CitaRepository;
import com.example.demo.hotelink.repository.ServicioRepository;
import com.example.demo.hotelink.repository.UsuarioRepository;

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

    @Autowired
    private EmailService emailService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public ResponseEntity<?> save(Cita c) {
        try {
            // Cargar usuario completo desde BD
            if (c.getUsuario() != null && c.getUsuario().getId() != null) {
                usuarioRepository.findById(c.getUsuario().getId())
                    .ifPresent(c::setUsuario);
            }

            Servicio servicioBD = servicioRepo.findById(c.getServicio().getId())
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

            System.out.println("=== CREAR CITA ===");
            System.out.println("Usuario: " + (c.getUsuario() != null ? c.getUsuario().getNombre() : "NULL"));
            System.out.println("Email: " + (c.getUsuario() != null ? c.getUsuario().getEmail() : "NULL"));
            System.out.println("Servicio: " + servicioBD.getNombre());

            int reservasActuales = repo.countByServicioIdAndFechaHoraCita(
                    servicioBD.getId(), c.getFechaHoraCita()
            );

            if (reservasActuales >= servicioBD.getAforoMaximo()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Lo sentimos, el aforo está completo para esa hora."));
            }

            Cita citaGuardada = repo.save(c);

            if (c.getUsuario() != null && c.getUsuario().getEmail() != null) {
                try {
                    emailService.enviarConfirmacionServicio(
                        c.getUsuario().getEmail(),
                        c.getUsuario().getNombre(),
                        servicioBD.getNombre(),
                        c.getFechaHoraCita().toLocalDate().toString(),
                        c.getFechaHoraCita().toLocalTime().toString(),
                        servicioBD.getPrecio()
                    );
                } catch (Exception e) {
                    System.err.println("Error enviando correo: " + e.getMessage());
                }
            }

            return ResponseEntity.ok(citaGuardada);

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
            Cita cita = repo.findById(id).orElse(null);
            if (cita == null)
                return ResponseEntity.status(404).body(Map.of("error", "La cita no existe"));

            // Guardamos datos antes de eliminar
            String email = cita.getUsuario() != null ? cita.getUsuario().getEmail() : null;
            String nombre = cita.getUsuario() != null ? cita.getUsuario().getNombre() : null;
            String servicio = cita.getServicio() != null ? cita.getServicio().getNombre() : "Servicio";
            String fecha = cita.getFechaHoraCita() != null ? cita.getFechaHoraCita().toLocalDate().toString() : "";

            repo.deleteById(id);

            // Enviar correo
            if (email != null) {
                try {
                    emailService.enviarAnulacionServicio(email, nombre, servicio, fecha);
                } catch (Exception e) {
                    System.err.println("Error enviando correo de anulación: " + e.getMessage());
                }
            }

            return ResponseEntity.ok(Map.of("mensaje", "Cita eliminada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al eliminar: " + e.getMessage()));
        }
    }

    public ResponseEntity<?> asignarEmpleado(Long citaId, Long empleadoId) {
        Cita cita = repo.findById(citaId).orElse(null);
        if (cita == null)
            return ResponseEntity.status(404).body(Map.of("error", "Cita no encontrada"));

        com.example.demo.hotelink.model.Usuario empleado = 
            new com.example.demo.hotelink.model.Usuario();
        empleado.setId(empleadoId);
        cita.setEmpleado(empleado);
        return ResponseEntity.ok(repo.save(cita));
    }

    
}