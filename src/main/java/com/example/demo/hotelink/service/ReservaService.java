package com.example.demo.hotelink.service;

import com.example.demo.hotelink.model.Reserva;
import com.example.demo.hotelink.model.TareaLimpieza;
import com.example.demo.hotelink.model.Factura;
import com.example.demo.hotelink.model.Habitacion;
import com.example.demo.hotelink.repository.ReservaRepository;
import com.example.demo.hotelink.repository.TareaLimpiezaRepository;
import com.example.demo.hotelink.repository.FacturaRepository;
import com.example.demo.hotelink.repository.HabitacionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository repo;

    @Autowired
    private HabitacionRepository habitacionRepo;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private TareaLimpiezaRepository tareaLimpiezaRepository;

    // Obtener todas
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(repo.findAll());
    }

    // Buscar por id
    public ResponseEntity<?> findById(Long id) {
        Reserva r = repo.findById(id).orElse(null);
        if (r == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Reserva no encontrada"));
        }
        return ResponseEntity.ok(r);
    }

    public List<Reserva> findByUsuarioId(Long usuarioId) {
        return repo.findByUsuarioId(usuarioId); 
    }

    // Guardar una nueva reserva
    public ResponseEntity<?> save(Reserva r) {
        try {
            if (r.getFechaEntrada() == null || r.getFechaSalida() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Las fechas son obligatorias"));
            }

            if (!r.getFechaSalida().isAfter(r.getFechaEntrada())) {
                return ResponseEntity.badRequest().body(Map.of("error", "La fecha de salida debe ser posterior"));
            }

            Long idHab = r.getHabitacion().getId();
            List<Reserva> solapes = repo.findByHabitacionIdAndFechas(idHab, r.getFechaEntrada(), r.getFechaSalida());

            if (!solapes.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "La habitación ya está ocupada en esas fechas"));
            }

            Habitacion h = habitacionRepo.findById(idHab).orElse(null);
            if (h != null) {
                LocalDate hoy = LocalDate.now();
                if (!r.getFechaSalida().isBefore(hoy) && !r.getFechaEntrada().isAfter(hoy)) {
                    h.setEstado("OCUPADA");
                    habitacionRepo.save(h);
                }
            }

            r.setEstado("CONFIRMADA"); // Estado inicial
            Reserva saved = repo.save(r);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al guardar reserva: " + e.getMessage()));
        }
    }

    // Eliminar física (Admin)
    public ResponseEntity<?> deleteById(Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("error", "Reserva no encontrada"));
        }
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Reserva eliminada correctamente"));
    }

    // --- MÉTODOS DE GESTIÓN (NUEVOS) ---

    // 1. Check-In
    public ResponseEntity<?> checkIn(Long id) {
        Reserva r = repo.findById(id).orElse(null);
        if (r == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Reserva no encontrada"));
        }
        r.setCheckIn(true);
        r.setEstado("CHECKIN"); 
        repo.save(r);
        return ResponseEntity.ok(Map.of("mensaje", "Check-In realizado con éxito"));
    }

    // 2. Check-Out
    public Factura realizarCheckOut(Long reservaId) {
        if (facturaRepository.existsByReservaId(reservaId)) {
            throw new RuntimeException("Esta reserva ya tiene una factura generada y ya se le hizo el Check-Out.");
        }
        Reserva reserva = repo.findById(reservaId).orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        long noches = ChronoUnit.DAYS.between(reserva.getFechaEntrada(), reserva.getFechaSalida());
        if (noches <= 0) noches = 1; 

        double totalPagar = noches * reserva.getHabitacion().getPrecio();

        Factura nuevaFactura = new Factura();
        nuevaFactura.setReserva(reserva);
        nuevaFactura.setUsuario(reserva.getUsuario()); 
        nuevaFactura.setFecha(LocalDate.now());        
        nuevaFactura.setTotal(totalPagar);             
        nuevaFactura.setEstado("PENDIENTE");           
        
        Factura facturaGuardada = facturaRepository.save(nuevaFactura);

        Habitacion habitacion = reserva.getHabitacion();
        habitacion.setEstado("LIMPIEZA"); 
        habitacionRepo.save(habitacion);

        TareaLimpieza nuevaTarea = new TareaLimpieza();
        nuevaTarea.setHabitacion(habitacion);
        nuevaTarea.setFecha(LocalDate.now());
        nuevaTarea.setEstado("PENDIENTE");
        tareaLimpiezaRepository.save(nuevaTarea);

        reserva.setCheckOut(true);
        reserva.setEstado("COMPLETADA"); 
        repo.save(reserva);

        return facturaGuardada;
    }

    // 3. Cancelar Reserva
    public ResponseEntity<?> cancelarReserva(Long id) {
        Reserva r = repo.findById(id).orElse(null);
        if (r == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Reserva no encontrada"));
        }
        
        // En lugar de borrarla de la BD, simplemente cambiamos su estado
        r.setEstado("CANCELADA");
        repo.save(r);
        
        return ResponseEntity.ok(Map.of("mensaje", "Reserva cancelada correctamente"));
    }

    public List<Habitacion> obtenerHabitacionesParaAdmin() {
        List<Habitacion> habitaciones = habitacionRepo.findAll();
        LocalDate hoy = LocalDate.now();

        for (Habitacion h : habitaciones) {
            // 1. Si hay reserva hoy, marcar como OCUPADA
            if (repo.existsByHabitacionIdAndFecha(h.getId(), hoy)) {
                h.setEstado("OCUPADA");
            } 
            // 2. Si no hay reserva y estaba ocupada, volver a LIBRE 
            // (a menos que esté en LIMPIEZA)
            else if ("OCUPADA".equals(h.getEstado())) {
                h.setEstado("LIBRE");
            }
            
            // Guardamos el cambio de estado en la BD
            habitacionRepo.save(h);
        }
        
        return habitaciones;
    }
}