package com.example.demo.hotelink.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el Cliente 
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Usuario usuario; 

    // Relación con el Servicio (Spa, Gimnasio, etc.)
    @ManyToOne
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    private LocalDateTime fechaHoraCita;

    private String estado;
}