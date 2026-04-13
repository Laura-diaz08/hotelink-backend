package com.example.demo.hotelink.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tareas_limpieza")
@Data
public class TareaLimpieza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String estado; // PENDIENTE, EN_PROCESO, COMPLETADA

    private LocalDate fecha;

    @ManyToOne
    private Habitacion habitacion;

    @ManyToOne
    private Usuario empleado; // rol LIMPIEZA
}

