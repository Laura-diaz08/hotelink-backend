package com.example.demo.hotelink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.time.LocalDate;


@Entity
@Table(name = "reservas")
@Data
public class Reserva {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDate fechaEntrada;

    @NotNull
    private LocalDate fechaSalida;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // quien hizo la reserva

    @ManyToOne
    @JoinColumn(name = "habitacion_id")
    private Habitacion habitacion;

    private Integer numeroHuespedes;

    private String estado; // CONFIRMADA, CHECKIN, CHECKOUT, CANCELADA
    
    private Boolean checkIn = false;
    
    private Boolean checkOut = false;

} 
