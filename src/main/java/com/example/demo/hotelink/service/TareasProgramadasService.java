package com.example.demo.hotelink.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.hotelink.model.Reserva;
import com.example.demo.hotelink.repository.CitaRepository;
import com.example.demo.hotelink.repository.ReservaRepository;

import jakarta.annotation.PostConstruct;

@Service
public class TareasProgramadasService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaService reservaService;

    
    @Autowired
    private CitaRepository citaRepository;

    // Se ejecuta todos los días a las 14:00
    @Scheduled(cron = "0 0 14 * * *")
    public void checkInAutomatico() {
        LocalDate hoy = LocalDate.now();
        
        // Busca reservas de hoy Y de días anteriores sin check-in
        List<Reserva> reservasPendientes = reservaRepository
            .findByFechaEntradaLessThanEqualAndEstado(hoy, "CONFIRMADA");
        
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


    // Se ejecuta cada hora
    @Scheduled(cron = "0 0 * * * *")
    public void completarCitasPasadas() {
        LocalDateTime ahora = LocalDateTime.now();
        
        List<com.example.demo.hotelink.model.Cita> citasPasadas = citaRepository
            .findByFechaHoraCitaBeforeAndEstadoNot(ahora, "COMPLETADA");
        
        for (com.example.demo.hotelink.model.Cita cita : citasPasadas) {
            cita.setEstado("COMPLETADA");
            citaRepository.save(cita);
            System.out.println("Cita " + cita.getId() + " marcada como COMPLETADA automáticamente");
        }
    }

    @PostConstruct
    public void ejecutarAlArrancar() {
        checkInAutomatico();
        checkOutAutomatico();
        completarCitasPasadas();
        System.out.println("✅ Tareas programadas ejecutadas al arrancar");
    }
}
