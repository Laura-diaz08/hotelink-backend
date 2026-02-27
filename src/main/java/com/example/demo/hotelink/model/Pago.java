package com.example.demo.hotelink.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pagos")
@Data
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double importe;

    private LocalDate fecha;

    private String metodo; // TARJETA, EFECTIVO

    @ManyToOne
    private Factura factura;
}

