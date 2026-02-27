package com.example.demo.hotelink.service;

import com.example.demo.hotelink.model.Reserva;
import com.example.demo.hotelink.model.Habitacion;
import com.example.demo.hotelink.repository.ReservaRepository;
import com.example.demo.hotelink.repository.HabitacionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository repo;

    @Autowired
    private HabitacionRepository habitacionRepo;

    //Obtener todas las reservas
    public ResponseEntity<?> findAll() {
        List<Reserva> lista = repo.findAll();

        if (lista.isEmpty()) {
            return ResponseEntity.status(404).body("No hay reservas registradas");
        }

        return ResponseEntity.ok(lista);
    }

    //Buscar reserva por id
    public ResponseEntity<?> findById(Long id) {
        Reserva r = repo.findById(id).orElse(null);

        if (r == null) {
            return ResponseEntity.status(404).body("Reserva no encontrada");
        }

        return ResponseEntity.ok(r);
    }

    //Guardar una nueva reserva
    public ResponseEntity<?> save(Reserva r) {
        try {
            //Validación de fechas
            if (r.getFechaEntrada() == null || r.getFechaSalida() == null) {
                return ResponseEntity.badRequest().body("Las fechas son obligatorias");
            }

            if (!r.getFechaSalida().isAfter(r.getFechaEntrada())) {
                return ResponseEntity.badRequest().body("La fecha de salida debe ser posterior a la fecha de entrada");
            }

            Long idHab = r.getHabitacion().getId();

            //Verificar solapamiento de reservas
            List<Reserva> solapes = repo.findByHabitacionIdAndFechaSalidaGreaterThanEqualAndFechaEntradaLessThanEqual(
                    idHab, r.getFechaEntrada(), r.getFechaSalida()
            );

            if (!solapes.isEmpty()) {
                return ResponseEntity.badRequest().body("La habitación ya está ocupada en esas fechas");
            }

            //Actualizar estado de la habitación a OCUPADA si la reserva incluye hoy
            Habitacion h = habitacionRepo.findById(idHab).orElse(null);
            if (h != null) {
                LocalDate hoy = LocalDate.now();
                if (!r.getFechaSalida().isBefore(hoy) && !r.getFechaEntrada().isAfter(hoy)) {
                    h.setEstado("OCUPADA");
                    habitacionRepo.save(h);
                }
            }

            Reserva saved = repo.save(r);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al guardar reserva: " + e.getMessage());
        }
    }

    //Eliminar reserva por id
    public ResponseEntity<?> deleteById(Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.status(404).body("Reserva no encontrada");
        }

        repo.deleteById(id);
        return ResponseEntity.ok("Reserva eliminada correctamente");
    }

    
    public ResponseEntity<?> checkIn(Long id) {

        Reserva r = repo.findById(id).orElse(null);

        if (r == null) {
            return ResponseEntity.status(404).body("Reserva no encontrada");
        }

        r.setCheckIn(true);
        repo.save(r);

        return ResponseEntity.ok(r);
    }

}
