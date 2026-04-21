package com.example.demo.hotelink.repository;

import com.example.demo.hotelink.model.ReservaServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaServicioRepository extends JpaRepository<ReservaServicio, Long> {
}