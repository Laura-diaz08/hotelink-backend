package com.example.demo.hotelink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.hotelink.model.Factura;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
    boolean existsByReservaId(Long reservaId);
}
