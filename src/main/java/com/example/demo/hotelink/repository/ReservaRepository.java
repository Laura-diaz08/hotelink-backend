package com.example.demo.hotelink.repository;

import com.example.demo.hotelink.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUsuarioId(Long usuarioId);

    // Le decimos a Spring Boot exactamente qué SQL ejecutar sin que intente adivinar
    @Query("SELECT r FROM Reserva r WHERE r.habitacion.id = :habitacionId AND " +
           "(r.fechaEntrada <= :fechaFin AND r.fechaSalida >= :fechaInicio)")
    List<Reserva> findByHabitacionIdAndFechas(
        @Param("habitacionId") Long habitacionId, 
        @Param("fechaInicio") LocalDate fechaInicio, 
        @Param("fechaFin") LocalDate fechaFin
    );

}

