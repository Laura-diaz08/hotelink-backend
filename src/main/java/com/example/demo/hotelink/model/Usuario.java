package com.example.demo.hotelink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 3, max = 80)
    @Column(unique = true)
    private String nombre; // nombre de usuario (login)

    @NotNull
    @Size(min = 4, max = 60)
    private String email;

    @NotNull
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull    
    private Rol rol; // "ADMIN" o "USER"

}
