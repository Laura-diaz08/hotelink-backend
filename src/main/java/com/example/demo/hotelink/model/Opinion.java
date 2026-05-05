package com.example.demo.hotelink.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "opiniones")
@Data
public class Opinion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private Integer estrellas; // 1 a 5

    @Column(length = 1000)
    private String comentario;

    private LocalDate fecha;
}