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

    @Query("SELECT COUNT(r) > 0 FROM Reserva r " +
           "WHERE r.habitacion.id = :habitacionId " +
           "AND :fecha >= r.fechaEntrada " +
           "AND :fecha < r.fechaSalida " +
           "AND r.estado <> 'CANCELADA'")
    boolean existsByHabitacionIdAndFecha(@Param("habitacionId") Long habitacionId, 
                                         @Param("fecha") LocalDate fecha);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reserva r " +
              "WHERE r.habitacion.id = :habitacionId AND :hoy BETWEEN r.fechaEntrada AND r.fechaSalida AND r.estado <> 'CANCELADA'")
       boolean existsByHabitacionIdAndFechaActual(@Param("habitacionId") Long habitacionId, @Param("hoy") LocalDate hoy);


       @Query("SELECT COUNT(DISTINCT r.habitacion) FROM Reserva r WHERE :hoy BETWEEN r.fechaEntrada AND r.fechaSalida AND r.estado <> 'CANCELADA'")
       long countHabitacionesOcupadasHoy(@Param("hoy") LocalDate hoy);

       List<Reserva> findByFechaEntradaAndEstado(LocalDate fechaEntrada, String estado);
       List<Reserva> findByFechaSalidaAndEstado(LocalDate fechaSalida, String estado);

       List<Reserva> findByFechaEntradaLessThanEqualAndEstado(LocalDate fecha, String estado);

}

