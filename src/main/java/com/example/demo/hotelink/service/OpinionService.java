package com.example.demo.hotelink.service;

import com.example.demo.hotelink.model.Opinion;
import com.example.demo.hotelink.model.Usuario;
import com.example.demo.hotelink.repository.OpinionRepository;
import com.example.demo.hotelink.repository.ReservaRepository;
import com.example.demo.hotelink.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpinionService {

    @Autowired
    private OpinionRepository repo;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Opinion> findAll() {
        return repo.findAll();
    }

    public ResponseEntity<?> crearOpinion(Long usuarioId, Integer estrellas, String comentario) {
        // Verificamos que el usuario tiene al menos una reserva completada
        boolean tieneReservaCompletada = reservaRepository
            .findByUsuarioId(usuarioId)
            .stream()
            .anyMatch(r -> "COMPLETADA".equals(r.getEstado()));

        if (!tieneReservaCompletada) {
            return ResponseEntity.status(403)
                .body(Map.of("error", "Solo puedes opinar si has completado una estancia"));
        }

        if (estrellas < 1 || estrellas > 5) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "La valoración debe ser entre 1 y 5 estrellas"));
        }

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }

        Opinion opinion = new Opinion();
        opinion.setUsuario(usuario);
        opinion.setEstrellas(estrellas);
        opinion.setComentario(comentario);
        opinion.setFecha(LocalDate.now());

        return ResponseEntity.ok(repo.save(opinion));
    }

    public ResponseEntity<?> eliminar(Long id) {
        if (!repo.existsById(id))
            return ResponseEntity.status(404).body(Map.of("error", "Opinión no encontrada"));
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Opinión eliminada"));
    }

    public Map<String, Object> getEstadisticas() {
        long total = repo.count();
        Double media = repo.calcularMediaEstrellas();

        Map<String, Long> distribucion = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            distribucion.put(i + " estrellas", repo.countByEstrellas(i));
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", total);
        stats.put("media", media != null ? Math.round(media * 10.0) / 10.0 : 0.0);
        stats.put("distribucion", distribucion);
        return stats;
    }

    public boolean usuarioPuedeOpinar(Long usuarioId) {
        return reservaRepository.findByUsuarioId(usuarioId)
            .stream()
            .anyMatch(r -> "COMPLETADA".equals(r.getEstado()));
    }
}