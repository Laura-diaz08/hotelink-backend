package com.example.demo.hotelink.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "cargos_reserva")
@Data
public class CargoReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @ManyToOne
    @JoinColumn(name = "articulo_id")
    private Articulo articulo;

    private Integer cantidad;

    private Double precioUnitario; // Precio en el momento del cargo

    private LocalDateTime fechaCargo;

    // Método helper para calcular el subtotal
    public Double getSubtotal() {
        if (precioUnitario == null || cantidad == null) return 0.0;
        return precioUnitario * cantidad;
    }
}