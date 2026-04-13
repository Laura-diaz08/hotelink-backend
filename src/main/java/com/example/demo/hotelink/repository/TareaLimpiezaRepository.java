package com.example.demo.hotelink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.hotelink.model.TareaLimpieza;

@Repository
public interface TareaLimpiezaRepository extends JpaRepository<TareaLimpieza, Long> {
    
}
