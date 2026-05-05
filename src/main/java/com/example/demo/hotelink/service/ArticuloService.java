package com.example.demo.hotelink.service;

import com.example.demo.hotelink.model.Articulo;
import com.example.demo.hotelink.model.CargoReserva;
import com.example.demo.hotelink.model.Reserva;
import com.example.demo.hotelink.repository.ArticuloRepository;
import com.example.demo.hotelink.repository.CargoReservaRepository;
import com.example.demo.hotelink.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ArticuloService {

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private CargoReservaRepository cargoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    // Obtener todos los artículos disponibles
    public List<Articulo> getArticulosDisponibles() {
        return articuloRepository.findByDisponibleTrue();
    }

    // Obtener todos los artículos (para admin)
    public List<Articulo> getTodos() {
        return articuloRepository.findAll();
    }

    // Añadir cargo a una reserva
    public ResponseEntity<?> añadirCargo(Long reservaId, Long articuloId, Integer cantidad) {
        Reserva reserva = reservaRepository.findById(reservaId).orElse(null);
        if (reserva == null)
            return ResponseEntity.status(404).body(Map.of("error", "Reserva no encontrada"));

        if (!"CONFIRMADA".equals(reserva.getEstado()) && !"CHECKIN".equals(reserva.getEstado()))
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Solo se pueden añadir cargos a reservas activas"));

        Articulo articulo = articuloRepository.findById(articuloId).orElse(null);
        if (articulo == null)
            return ResponseEntity.status(404).body(Map.of("error", "Artículo no encontrado"));

        if (!articulo.getDisponible())
            return ResponseEntity.badRequest().body(Map.of("error", "Artículo no disponible"));

        CargoReserva cargo = new CargoReserva();
        cargo.setReserva(reserva);
        cargo.setArticulo(articulo);
        cargo.setCantidad(cantidad);
        cargo.setPrecioUnitario(articulo.getPrecio());
        cargo.setFechaCargo(LocalDateTime.now());

        return ResponseEntity.ok(cargoRepository.save(cargo));
    }

    // Obtener cargos de una reserva
    public List<CargoReserva> getCargosDeReserva(Long reservaId) {
        return cargoRepository.findByReservaId(reservaId);
    }

    // Eliminar cargo
    public ResponseEntity<?> eliminarCargo(Long cargoId) {
        if (!cargoRepository.existsById(cargoId))
            return ResponseEntity.status(404).body(Map.of("error", "Cargo no encontrado"));
        cargoRepository.deleteById(cargoId);
        return ResponseEntity.ok(Map.of("mensaje", "Cargo eliminado"));
    }

    // Calcular total de cargos de una reserva
    public Double getTotalCargos(Long reservaId) {
        Double total = cargoRepository.calcularTotalCargos(reservaId);
        return total != null ? total : 0.0;
    }
}