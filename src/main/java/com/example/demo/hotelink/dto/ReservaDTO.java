package com.example.demo.hotelink.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReservaDTO {
    private Long id;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private String estado;
    private Boolean checkIn;
    private Boolean checkOut;
    private Integer numeroHuespedes;
    private String numeroHabitacion;
    private String tipoHabitacion;
    private String nombreUsuario;
    private Double total;
}