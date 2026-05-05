package com.example.demo.hotelink.repository;

import com.example.demo.hotelink.model.Opinion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface OpinionRepository extends JpaRepository<Opinion, Long> {
    
    List<Opinion> findByUsuarioId(Long usuarioId);
    
    // Media de estrellas
    @Query("SELECT AVG(o.estrellas) FROM Opinion o")
    Double calcularMediaEstrellas();
    
    // Contar por número de estrellas
    Long countByEstrellas(Integer estrellas);
}