package com.example.demo.hotelink.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.hotelink.model.Rol;
import com.example.demo.hotelink.model.TareaLimpieza;
import com.example.demo.hotelink.model.Usuario;
import com.example.demo.hotelink.service.TareaLimpiezaService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tareas-limpieza")
@CrossOrigin(origins = "*") // Imprescindible para Angular
public class TareaLimpiezaController {

    @Autowired
    private TareaLimpiezaService tareaService;

    @Autowired
    private com.example.demo.hotelink.repository.UsuarioRepository usuarioRepository;

    // Angular llamará aquí para pintar la tabla
    @GetMapping
    public List<TareaLimpieza> listarTareas() {
        return tareaService.obtenerTodas();
    }

    // Cambiar estado de una tarea
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return tareaService.cambiarEstado(id, body.get("estado"));
    }

    // Asignar empleado a una tarea
    @PutMapping("/{id}/asignar")
    public ResponseEntity<?> asignarEmpleado(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        return tareaService.asignarEmpleado(id, body.get("empleadoId"));
    }

    // Crear tarea manual
    @PostMapping
    public ResponseEntity<?> crearTarea(@RequestBody Map<String, Object> body) {
        return tareaService.crearTareaManual(
            Long.valueOf(body.get("habitacionId").toString()),
            body.get("fecha").toString()
        );
    }

    // Eliminar tarea
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        return tareaService.eliminar(id);
    }

    @GetMapping("/empleados")
    public ResponseEntity<?> getEmpleadosLimpieza() {
        List<Usuario> empleados = usuarioRepository.findByRol(Rol.LIMPIEZA);
        return ResponseEntity.ok(empleados);
    }
}
