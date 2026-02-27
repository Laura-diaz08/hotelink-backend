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

    private String tipo; // sencilla, doble, suite...

    private Double precio;

    private String estado; // LIBRE, OCUPADA, LIMPIEZA, FUERA_SERVICIO
    
    @ManyToOne
    @JoinColumn(name = "cliente_id") // Esta será la columna en la base de datos
    private Usuario cliente; 
}