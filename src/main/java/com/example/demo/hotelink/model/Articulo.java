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

    private String nombre;

    private String descripcion;

    // CUNA, CAMA_EXTRA, MINIBAR, DESAYUNO, VINO, DECORACION
    private String categoria;

    private Double precio;

    private Boolean disponible = true;
}