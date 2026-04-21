package com.example.demo.hotelink.repository;

import com.example.demo.hotelink.model.Cita;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByUsuarioId(Long usuarioId);
    int countByServicioIdAndFechaHoraCita(Long servicioId, LocalDateTime fechaHoraCita);
}