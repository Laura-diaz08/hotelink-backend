package com.example.demo.hotelink.service; // Pon tu paquete correcto

import com.example.demo.hotelink.model.Habitacion;
import com.example.demo.hotelink.model.Rol;
import com.example.demo.hotelink.model.TareaLimpieza;
import com.example.demo.hotelink.model.Usuario;
import com.example.demo.hotelink.repository.TareaLimpiezaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TareaLimpiezaService {

    @Autowired
    private TareaLimpiezaRepository tareaRepository;

    @Autowired
    private com.example.demo.hotelink.repository.UsuarioRepository usuarioRepository;

    @Autowired
    private com.example.demo.hotelink.repository.HabitacionRepository habitacionRepository;

    public List<TareaLimpieza> obtenerTodas() {
        return tareaRepository.findAll();
    }

    // El método que llama el ReservaService al hacer Check-Out
    public void crearTareaParaHabitacion(Habitacion habitacion) {
        TareaLimpieza nuevaTarea = new TareaLimpieza();
        
        nuevaTarea.setHabitacion(habitacion);
        nuevaTarea.setFecha(LocalDate.now());
        nuevaTarea.setEstado("PENDIENTE");
        
        tareaRepository.save(nuevaTarea);
    }

    public ResponseEntity<?> cambiarEstado(Long id, String nuevoEstado) {
        TareaLimpieza tarea = tareaRepository.findById(id).orElse(null);
        if (tarea == null) return ResponseEntity.status(404).body("Tarea no encontrada");
        tarea.setEstado(nuevoEstado);
        return ResponseEntity.ok(tareaRepository.save(tarea));
    }

    public ResponseEntity<?> asignarEmpleado(Long id, Long empleadoId) {
        TareaLimpieza tarea = tareaRepository.findById(id).orElse(null);
        if (tarea == null) return ResponseEntity.status(404).body("Tarea no encontrada");

        com.example.demo.hotelink.model.Usuario empleado = usuarioRepository.findById(empleadoId).orElse(null);
        if (empleado == null) return ResponseEntity.status(404).body("Empleado no encontrado");

        tarea.setEmpleado(empleado);
        return ResponseEntity.ok(tareaRepository.save(tarea));
    }

    public ResponseEntity<?> crearTareaManual(Long habitacionId, String fechaStr) {
        Habitacion habitacion = habitacionRepository.findById(habitacionId).orElse(null);
        if (habitacion == null) return ResponseEntity.status(404).body("Habitación no encontrada");

        TareaLimpieza tarea = new TareaLimpieza();
        tarea.setHabitacion(habitacion);
        tarea.setFecha(LocalDate.parse(fechaStr));
        tarea.setEstado("PENDIENTE");
        return ResponseEntity.ok(tareaRepository.save(tarea));
    }

    public ResponseEntity<?> eliminar(Long id) {
        if (!tareaRepository.existsById(id)) 
            return ResponseEntity.status(404).body("Tarea no encontrada");
        tareaRepository.deleteById(id);
        return ResponseEntity.ok("Tarea eliminada");
    }

    public List<Usuario> getEmpleadosLimpieza() {
        return usuarioRepository.findByRol(Rol.LIMPIEZA);
    }
}