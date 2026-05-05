package com.example.demo.hotelink.repository;

import com.example.demo.hotelink.model.Articulo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticuloRepository extends JpaRepository<Articulo, Long> {

    List<Articulo> findByDisponibleTrue();
    List<Articulo> findByCategoria(String categoria);
}