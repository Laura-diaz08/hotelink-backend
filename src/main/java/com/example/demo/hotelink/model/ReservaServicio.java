package com.example.demo.hotelink.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservas_servicios")
@Data
public class ReservaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Usuario empleado;

    private LocalDateTime fechaHoraCita;
    
    private String estado; // PENDIENTE, COMPLETADA, CANCELADA
}
