package com.example.demo.hotelink.controller;

import com.example.demo.hotelink.auth.JwtService;
import com.example.demo.hotelink.model.Habitacion;
import com.example.demo.hotelink.model.Reserva;
import com.example.demo.hotelink.repository.HabitacionRepository;
import com.example.demo.hotelink.repository.ReservaRepository;
import com.example.demo.hotelink.service.HabitacionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/habitaciones")
@CrossOrigin(origins = "*")
public class HabitacionController {

    @Autowired
    private HabitacionService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private HabitacionRepository repo;

    @Autowired
    private ReservaRepository reservaRepository;

    // --- ENDPOINTS PÚBLICOS / USUARIOS ---

    /**
     * Obtiene todas las habitaciones con su estado actualizado (LIBRE/OCUPADA)
     * según la fecha actual.
     */
    @GetMapping
    public ResponseEntity<?> findAll(@RequestHeader(name="Authorization", required=false) String auth) {
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        // Usamos el método que actualiza estados automáticamente
        return ResponseEntity.ok(service.obtenerHabitacionesActualizadas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@RequestHeader(name="Authorization", required=false) String auth,
                                      @PathVariable Long id) {
        if (!jwtService.usuarioValido(auth))
            return ResponseEntity.status(401).body(Map.of("error","Token inválido"));

        return service.findById(id);
    }

    /**
     * Buscar habitaciones que no tengan reservas en un rango de fechas.
     */
    @GetMapping("/disponibles")
    public ResponseEntity<?> buscarDisponibles(
            @RequestParam String tipo,
            @RequestParam String entrada,
            @RequestParam String salida) {


        LocalDate fechaEntrada = LocalDate.parse(entrada);
        LocalDate fechaSalida = LocalDate.parse(salida);

        List<Habitacion> todasDelTipo = repo.findByTipo(tipo);

        List<Habitacion> disponibles = todasDelTipo.stream()
            .filter(h -> !h.getEstado().equals("MANTENIMIENTO"))
            .filter(h -> {
                List<Reserva> solapes = reservaRepository.findByHabitacionIdAndFechas(
                    h.getId(), fechaEntrada, fechaSalida
                );
                return solapes.isEmpty();
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "tipo", tipo,
            "disponibles", disponibles.size(),
            "precio", disponibles.isEmpty() ? 0 : disponibles.get(0).getPrecio(),
            "capacidad", disponibles.isEmpty() ? 0 : disponibles.get(0).getCapacidad()
        ));
    }

    /**
     * El usuario crea su propia reserva (Asignación automática)
     */
    @PostMapping("/{id}/reservar")
    public ResponseEntity<?> reservarHabitacion(
            @RequestHeader(name="Authorization", required=false) String auth,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        
        try {
            if (!jwtService.usuarioValido(auth)) {
                return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión para reservar"));
            }

            if (body.get("clienteId") == null || body.get("fechaInicio") == null || body.get("fechaFin") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Faltan datos en la reserva"));
            }

            Long clienteId = Long.valueOf(body.get("clienteId").toString());
            String fechaInicio = body.get("fechaInicio").toString();
            String fechaFin = body.get("fechaFin").toString();
            Integer numHuespedes = body.get("numeroHuespedes") != null ? 
                                   Integer.valueOf(body.get("numeroHuespedes").toString()) : 1;

            return service.crearReserva(id, clienteId, fechaInicio, fechaFin, numHuespedes);
            
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(500).body(Map.of("error", "Error en la reserva: " + e.getMessage()));
        }
    }

    // --- ENDPOINTS EXCLUSIVOS ADMIN ---

    /**
     * Gestión total para el Administrador.
     */
    @GetMapping("/admin/gestion")
    public ResponseEntity<List<Habitacion>> getHabitacionesAdmin(@RequestHeader(name="Authorization", required=false) String auth) {
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).build();

        return ResponseEntity.ok(service.obtenerHabitacionesActualizadas());
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestHeader(name="Authorization", required=false) String auth,
                                  @RequestBody Habitacion h) {
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        return service.save(h);
    }

    /**
     * El admin solo cambia estados manuales (como LIMPIEZA o MANTENIMIENTO).
     * El estado OCUPADA se gestiona solo por fechas.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@RequestHeader(name="Authorization", required=false) String auth,
                                           @PathVariable Long id,
                                           @RequestBody Map<String,String> body) {
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        return service.cambiarEstado(id, body.get("estado"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader(name="Authorization", required=false) String auth,
                                    @PathVariable Long id) {
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        return service.deleteById(id);
    }

    @GetMapping("/admin/resumen")
    public ResponseEntity<?> getResumen(@RequestHeader(name="Authorization", required=false) String auth) {

        System.out.println("Token recibido en el Backend: " + auth);

        if (!jwtService.adminValido(auth)) {
            System.out.println("El token no es válido o no pertenece a un ADMIN.");
            return ResponseEntity.status(403).build();
        }
            
        return ResponseEntity.ok(service.obtenerResumenDashboard());
    }

    @GetMapping("/actualizadas")
    public ResponseEntity<List<Habitacion>> getHabitacionesActualizadas() {

        List<Habitacion> lista = service.obtenerHabitacionesActualizadas();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/admin/debug")
    public ResponseEntity<?> debug(@RequestHeader(name="Authorization") String auth) {
        if (!jwtService.adminValido(auth)) 
            return ResponseEntity.status(403).build();
            
        // Devuelve el conteo real y el número total de habitaciones detectadas por el servicio
        return ResponseEntity.ok(Map.of(
            "total_repo", service.obtenerResumenDashboard().get("total"),
            "ocupadas_repo", service.obtenerResumenDashboard().get("ocupadas")
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestHeader(name="Authorization", required=false) String auth,
                                    @PathVariable Long id,
                                    @RequestBody Habitacion h) {
        if (!jwtService.adminValido(auth))
            return ResponseEntity.status(403).body(Map.of("error","Solo ADMIN"));

        h.setId(id);
        return service.save(h);
    }

}