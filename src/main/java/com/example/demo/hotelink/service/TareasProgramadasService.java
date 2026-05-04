package com.example.demo.hotelink.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.hotelink.model.Reserva;
import com.example.demo.hotelink.repository.ReservaRepository;

@Service
public class TareasProgramadasService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaService reservaService;

    // Se ejecuta todos los días a las 14:00
    @Scheduled(cron = "0 0 14 * * *")
    public void checkInAutomatico() {
        LocalDate hoy = LocalDate.now();
        
        List<Reserva> reservasPendientes = reservaRepository
            .findByFechaEntradaAndEstado(hoy, "CONFIRMADA");
        
        for (Reserva r : reservasPendientes) {
            if (!r.getCheckIn()) {
                reservaService.checkIn(r.getId());
                System.out.println("Check-In automático realizado para reserva: " + r.getId());
            }
        }
    }

    // Se ejecuta todos los días a las 14:00
    @Scheduled(cron = "0 0 14 * * *")
    public void checkOutAutomatico() {
        LocalDate hoy = LocalDate.now();
        
        List<Reserva> reservasConCheckIn = reservaRepository
            .findByFechaSalidaAndEstado(hoy, "CHECKIN");
        
        for (Reserva r : reservasConCheckIn) {
            if (!r.getCheckOut()) {
                try {
                    reservaService.realizarCheckOut(r.getId());
                    System.out.println("Check-Out automático realizado para reserva: " + r.getId());
                } catch (Exception e) {
                    System.out.println("Error en checkout automático reserva " + r.getId() + ": " + e.getMessage());
                }
            }
        }
    }
}
