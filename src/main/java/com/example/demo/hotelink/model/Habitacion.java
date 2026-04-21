package com.example.demo.hotelink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "habitaciones")
@Data
public class Habitacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String numero;

    private String tipo; 

    private Double precio;

    private Integer capacidad; 

    @Column(length = 500)
    private String descripcion;

    private String estado; // LIBRE, OCUPADA, LIMPIEZA, FUERA_SERVICIO
    
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Usuario cliente; 
}