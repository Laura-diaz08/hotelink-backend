package com.example.demo.hotelink.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "facturas")
@Data
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    private Double total;

    private String estado; // PENDIENTE, PAGADA

    @ManyToOne
    private Usuario usuario;

    @OneToOne
    private Reserva reserva;
}

