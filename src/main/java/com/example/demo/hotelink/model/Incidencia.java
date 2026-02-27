package com.example.demo.hotelink.model;

import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;


@Entity
@Table(name = "incidencias")
@Data
public class Incidencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;

    private String estado; // ABIERTA, EN_PROGRESO, RESUELTA

    private LocalDateTime fechaCreacion;

    @ManyToOne
    @JoinColumn(name = "habitacion_id")
    private Habitacion habitacion;


    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private Usuario responsable; // opcional
}
