package com.example.demo.hotelink.model;

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

    @ManyToOne
    private Habitacion habitacion;

    @ManyToOne
    private Usuario empleado; // rol LIMPIEZA
}

