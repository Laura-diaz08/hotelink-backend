package com.example.demo.hotelink.service;

import com.example.demo.hotelink.model.Usuario;
import com.example.demo.hotelink.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repo;

    // @Autowired
    // private HabitacionRepository habitacionRepo;

    //Buscar usuario por nombre
    public Usuario buscarPorNombre(String nombre) {
        return repo.findByNombre(nombre);
    }

    //Guardar un usuario
    public Usuario guardar(Usuario u) {
        return repo.save(u);
    }

    //Obtener todos los usuarios
    public ResponseEntity<?> findAll() {
        List<Usuario> lista = repo.findAll();

        if (lista.isEmpty()) {
            return ResponseEntity.status(404).body("No hay usuarios registrados");
        }

        return ResponseEntity.ok(lista);
    }

    //Buscar usuario por id
    public ResponseEntity<?> findById(Long id) {
        Usuario u = repo.findById(id).orElse(null);

        if (u == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }

        return ResponseEntity.ok(u);
    }

    //Guardar o actualizar usuario
    public ResponseEntity<?> save(Usuario u) {
        try {
            Usuario guardado = repo.save(u);
            return ResponseEntity.ok(guardado);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error guardando usuario: " + e.getMessage());
        }
    }

    //Eliminar usuario por id
    public ResponseEntity<?> deleteById(Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.status(404).body("Usuario no existe");
        }

        repo.deleteById(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    //Verifica si no hay usuarios en la base de datos
    public boolean noHayUsuarios() {
        return repo.count() == 0;
    }


    public ResponseEntity<?> update(Long id, Usuario u) {
        Usuario existente = repo.findById(id).orElse(null);
        if (existente == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }

        // Actualizamos solo los campos editables, mantenemos el rol y la password si no viene nueva
        existente.setNombre(u.getNombre());
        existente.setEmail(u.getEmail());
        
        // Solo actualizamos la password si viene una nueva
        if (u.getPassword() != null && !u.getPassword().isBlank()) {
            existente.setPassword(u.getPassword());
        }

        try {
            return ResponseEntity.ok(repo.save(existente));
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error actualizando usuario: " + e.getMessage());
        }
    }
}
