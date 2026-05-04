package com.example.demo.hotelink.repository;

import com.example.demo.hotelink.model.Habitacion;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {
    Habitacion findByNumero(String numero);

    // Busca todas las habitaciones asignadas a un cliente concreto
    List<Habitacion> findByClienteId(Long clienteId);

    // Busca habitaciones libres (que no tengan reservas solapadas y no estén en mantenimiento)
    @Query("SELECT h FROM Habitacion h WHERE h.estado != 'MANTENIMIENTO' AND h.id NOT IN " +
           "(SELECT r.habitacion.id FROM Reserva r WHERE r.estado != 'CANCELADA' " +
           "AND r.fechaEntrada < :fin AND r.fechaSalida > :inicio)")
    List<Habitacion> findDisponiblesPorFechas(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    @Query("SELECT COUNT(h) FROM Habitacion h WHERE TRIM(UPPER(h.estado)) = TRIM(UPPER(:estado))")
    long countByEstado(@Param("estado") String estado);
    
}
