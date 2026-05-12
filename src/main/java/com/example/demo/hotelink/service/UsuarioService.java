package com.example.demo.hotelink.service;

import com.example.demo.hotelink.model.Usuario;
import com.example.demo.hotelink.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario buscarPorNombre(String nombre) {
        return repo.findByNombre(nombre).orElse(null);
    }

    public Usuario guardar(Usuario u) {
        return repo.save(u);
    }

    public ResponseEntity<?> findAll() {
        List<Usuario> lista = repo.findAll();

        if (lista.isEmpty())
            return ResponseEntity.status(404).body("No hay usuarios registrados");
        return ResponseEntity.ok(lista);
    }

    public ResponseEntity<?> findById(Long id) {
        Usuario u = repo.findById(id).orElse(null);
        
        if (u == null)
            return ResponseEntity.status(404).body("Usuario no encontrado");
        return ResponseEntity.ok(u);
    }

    public ResponseEntity<?> save(Usuario u) {
        try {
            // Encriptar password
            if (u.getPassword() != null && !u.getPassword().isBlank()) {
                u.setPassword(passwordEncoder.encode(u.getPassword()));
            }

            // Generar código de verificación y marcar como no verificado
            String codigo = String.valueOf((int)(Math.random() * 900000) + 100000);
            u.setCodigoVerificacion(codigo);
            u.setVerificado(false);

            Usuario guardado = repo.save(u);

            // Enviar correo de verificación
            try {
                emailService.enviarCodigoVerificacion(
                    guardado.getEmail(),
                    guardado.getNombre(),
                    codigo
                );
            } catch (Exception e) {
                System.err.println("Error enviando correo: " + e.getMessage());
            }

            return ResponseEntity.ok(guardado);

        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error guardando usuario: " + e.getMessage());
        }
    }

    public ResponseEntity<?> deleteById(Long id) {
        if (!repo.existsById(id))
            return ResponseEntity.status(404).body("Usuario no existe");

        repo.deleteById(id);

        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    public boolean noHayUsuarios() {
        return repo.count() == 0;
    }

    public ResponseEntity<?> update(Long id, Usuario u) {
        Usuario existente = repo.findById(id).orElse(null);

        if (existente == null)
            return ResponseEntity.status(404).body("Usuario no encontrado");

        existente.setNombre(u.getNombre());
        existente.setEmail(u.getEmail());

        if (u.getPassword() != null && !u.getPassword().isBlank())
            existente.setPassword(u.getPassword());

        try {
            return ResponseEntity.ok(repo.save(existente));
            
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error actualizando usuario: " + e.getMessage());
        }
    }
}