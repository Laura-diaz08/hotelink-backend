package com.example.demo.hotelink.repository;

import com.example.demo.hotelink.model.Cita;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByUsuarioId(Long usuarioId);

    int countByServicioIdAndFechaHoraCita(Long servicioId, LocalDateTime fechaHoraCita);

    List<Cita> findByFechaHoraCitaBeforeAndEstadoNot(LocalDateTime fecha, String estado);

    List<Cita> findByUsuarioIdAndFechaHoraCitaBetween(Long usuarioId, LocalDateTime inicio, LocalDateTime fin);

    List<Cita> findByFechaHoraCitaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Cita> findByEmpleadoIsNullAndEstadoNot(String estado);
    
    List<Cita> findByEmpleadoId(Long empleadoId);

    @Query("SELECT c FROM Cita c WHERE c.empleado IS NULL AND (c.estado IS NULL OR c.estado != 'COMPLETADA') AND c.servicio.requiereEmpleado = true")
    List<Cita> findCitasDisponibles();

    @Query("SELECT c FROM Cita c WHERE c.empleado IS NULL AND (c.estado IS NULL OR c.estado != 'COMPLETADA') AND c.servicio.requiereEmpleado = true AND c.servicio.rolRequerido = :rol")
    List<Cita> findCitasDisponiblesPorRol(@Param("rol") String rol);
}