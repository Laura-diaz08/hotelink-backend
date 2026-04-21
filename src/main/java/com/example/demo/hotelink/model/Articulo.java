package com.example.demo.hotelink.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "articulos")
@Data
public class Articulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // Ej: "Coca-Cola Minibar", "Toalla Extra", "Desayuno Buffet"
    
    // Puede ser "MINIBAR", "RESTAURANTE", "PETICION_HABITACION"
    private String categoria; 
    
    // El precio que se sumará a la factura. (Una toalla extra podría ser 0.0)
    private Double precio; 
}