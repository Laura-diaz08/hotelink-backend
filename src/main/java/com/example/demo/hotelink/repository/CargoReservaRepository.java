package com.example.demo.hotelink.repository;

import com.example.demo.hotelink.model.CargoReserva;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CargoReservaRepository extends JpaRepository<CargoReserva, Long> {

    List<CargoReserva> findByReservaId(Long reservaId);
    
    @Query("SELECT SUM(c.precioUnitario * c.cantidad) FROM CargoReserva c WHERE c.reserva.id = :reservaId")
    Double calcularTotalCargos(@Param("reservaId") Long reservaId);
}