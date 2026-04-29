package com.example.demo.hotelink.service;

import com.example.demo.hotelink.model.Habitacion;
import com.example.demo.hotelink.model.Reserva;
import com.example.demo.hotelink.model.Usuario;
import com.example.demo.hotelink.repository.HabitacionRepository;
import com.example.demo.hotelink.repository.ReservaRepository;
import com.example.demo.hotelink.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class HabitacionService {

    @Autowired
    private HabitacionRepository repo;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    private static final List<String> ESTADOS_VALIDOS = List.of("LIBRE", "OCUPADA", "MANTENIMIENTO");

    //Obtener todas las habitaciones
    public ResponseEntity<?> findAll() {
        List<Habitacion> lista = repo.findAll();

        if (lista.isEmpty()) {
            return ResponseEntity.status(404).body("No hay habitaciones registradas");
        }

        return ResponseEntity.ok(lista);
    }

    //Buscar habitación por id
    public ResponseEntity<?> findById(Long id) {
        Habitacion h = repo.findById(id).orElse(null);

        if (h == null) {
            return ResponseEntity.status(404).body("Habitación no encontrada");
        }

        return ResponseEntity.ok(h);
    }

    //Guardar o actualizar habitación
    public ResponseEntity<?> save(Habitacion h) {
        try {
            //Número de habitación único
            Habitacion existente = repo.findByNumero(h.getNumero());
            if (existente != null && !existente.getId().equals(h.getId())) {
                return ResponseEntity.status(409).body("Ya existe una habitación con ese número");
            }

            //Estado por defecto
            if (h.getEstado() == null || h.getEstado().isBlank()) {
                h.setEstado("LIBRE");
            }

            //Validar estado
            if (!ESTADOS_VALIDOS.contains(h.getEstado().toUpperCase())) {
                return ResponseEntity.badRequest()
                        .body("Estado inválido. Estados válidos: " + ESTADOS_VALIDOS);
            }

            h.setEstado(h.getEstado().toUpperCase());
            return ResponseEntity.ok(repo.save(h));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al guardar la habitación: " + e.getMessage());
        }
    }

    //Cambiar estado de una habitación
    public ResponseEntity<?> cambiarEstado(Long id, String nuevoEstado) {
        Habitacion h = repo.findById(id).orElse(null);

        if (h == null) {
            return ResponseEntity.status(404).body("Habitación no encontrada");
        }

        if (!ESTADOS_VALIDOS.contains(nuevoEstado.toUpperCase())) {
            return ResponseEntity.badRequest()
                    .body("Estado inválido. Estados permitidos: " + ESTADOS_VALIDOS);
        }

        h.setEstado(nuevoEstado.toUpperCase());
        repo.save(h);

        return ResponseEntity.ok(h);
    }

    public ResponseEntity<?> asignarCliente(Long idHabitacion, Long idCliente) {
        // 1. Buscamos la habitación
        Optional<Habitacion> habOpt = repo.findById(idHabitacion);
        if (habOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Habitación no encontrada"));
        }
        Habitacion habitacion = habOpt.get();

        // 2. Si el idCliente es nulo, significa que queremos LIBERAR la habitación
        if (idCliente == null) {
            habitacion.setCliente(null);
            habitacion.setEstado("LIBRE");
            repo.save(habitacion);
            return ResponseEntity.ok(habitacion);
        }

        // 3. Buscamos al cliente
        Optional<Usuario> clienteOpt = usuarioRepository.findById(idCliente);
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Cliente no encontrado"));
        }
        Usuario cliente = clienteOpt.get();

        // 4. Hacemos la asignación
        habitacion.setCliente(cliente);
        habitacion.setEstado("OCUPADA"); // Cambiamos el estado automáticamente
        
        repo.save(habitacion);
        return ResponseEntity.ok(habitacion);
    }

    //Eliminar habitación por id
    public ResponseEntity<?> deleteById(Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.status(404).body("Habitación no encontrada");
        }

        repo.deleteById(id);
        return ResponseEntity.ok("Habitación eliminada correctamente");
    }

    public ResponseEntity<?> obtenerHabitacionesDeCliente(Long clienteId) {
        List<Habitacion> misHabitaciones = repo.findByClienteId(clienteId);
        return ResponseEntity.ok(misHabitaciones);
    }

    // 1. Método para buscar las habitaciones disponibles
    public ResponseEntity<?> findDisponiblesPorFechas(String inicioStr, String finStr) {
        try {
            LocalDate inicio = LocalDate.parse(inicioStr);
            LocalDate fin = LocalDate.parse(finStr);
            
            // Usamos la consulta mágica del repositorio
            List<Habitacion> disponibles = repo.findDisponiblesPorFechas(inicio, fin);
            return ResponseEntity.ok(disponibles);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Formato de fecha inválido. Usa AAAA-MM-DD"));
        }
    }

    // 2. Método para crear la reserva de la habitacion
    public ResponseEntity<?> crearReserva(Long habitacionId, Long clienteId, String fechaInicioStr, String fechaFinStr, Integer numeroHuespedes) {
        try {
            LocalDate inicio = LocalDate.parse(fechaInicioStr);
            LocalDate fin = LocalDate.parse(fechaFinStr);

            // 1. Buscamos la habitación y el usuario en la BD
            Habitacion habitacion = repo.findById(habitacionId).orElse(null);
            Usuario usuario = usuarioRepository.findById(clienteId).orElse(null);

            if (habitacion == null || usuario == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Habitación o usuario no encontrados"));
            }

            // 2. (Opcional pero recomendado) Comprobar de nuevo si está libre por seguridad
            List<Habitacion> libres = repo.findDisponiblesPorFechas(inicio, fin);
            if (!libres.contains(habitacion)) {
                return ResponseEntity.badRequest().body(Map.of("error", "La habitación ya no está disponible en esas fechas"));
            }

            // 3. Creamos y rellenamos la reserva
            Reserva nuevaReserva = new Reserva();
            nuevaReserva.setHabitacion(habitacion);
            nuevaReserva.setUsuario(usuario);
            nuevaReserva.setFechaEntrada(inicio);
            nuevaReserva.setFechaSalida(fin);
            nuevaReserva.setEstado("CONFIRMADA");
            nuevaReserva.setNumeroHuespedes(numeroHuespedes);
            
            // 4. Guardamos en la base de datos
            reservaRepository.save(nuevaReserva);

            return ResponseEntity.ok(Map.of("mensaje", "¡Reserva completada con éxito!", "reservaId", nuevaReserva.getId()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al procesar la reserva"));
        }
    }

    public List<Habitacion> obtenerHabitacionesActualizadas() {
        List<Habitacion> habitaciones = repo.findAll();
        LocalDate hoy = LocalDate.now();

        for (Habitacion h : habitaciones) {
            // Buscamos si hay una reserva activa para HOY en esta habitación
            boolean estaReservadaHoy = reservaRepository.existsByHabitacionIdAndFecha(h.getId(), hoy);
            
            if (estaReservadaHoy) {
                h.setEstado("OCUPADA");
            } else if (!h.getEstado().equals("LIMPIEZA")) { 
                h.setEstado("LIBRE");
            }
            repo.save(h);
        }
        return habitaciones;
    }
}
