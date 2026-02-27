package com.example.demo.hotelink.repository;

import com.example.demo.hotelink.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    // Busca reservas de una habitación que solapan con un rango dado (validar disponibilidad)
    List<Reserva> findByHabitacionIdAndFechaSalidaGreaterThanEqualAndFechaEntradaLessThanEqual(
            Long habitacionId, LocalDate fechaEntrada, LocalDate fechaSalida);

    // Buscar reservas activas en una fecha concreta
    List<Reserva> findByHabitacionIdAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
            Long habitacionId, LocalDate fecha, LocalDate fecha2);
}

