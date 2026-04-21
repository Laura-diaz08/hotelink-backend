package com.example.demo.hotelink.service;

import com.example.demo.hotelink.model.ReservaServicio;
import com.example.demo.hotelink.repository.ReservaServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReservaServicioService {

    @Autowired
    private ReservaServicioRepository repository;

    // Para que el trabajador vea TODAS las citas del hotel
    public List<ReservaServicio> obtenerTodas() {
        return repository.findAll();
    }

    // Para que un trabajador vea solo SUS citas asignadas
    public List<ReservaServicio> obtenerPorEmpleado(Long empleadoId) {
        return repository.findAll().stream()
                .filter(r -> r.getEmpleado() != null && r.getEmpleado().getId().equals(empleadoId))
                .toList();
    }

    public ReservaServicio guardar(ReservaServicio reserva) {
        return repository.save(reserva);
    }
}