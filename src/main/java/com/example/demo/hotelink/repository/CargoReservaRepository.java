package com.example.demo.hotelink.repository;

import com.example.demo.hotelink.model.CargoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CargoReservaRepository extends JpaRepository<CargoReserva, Long> {
}