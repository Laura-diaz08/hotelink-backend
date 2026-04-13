package com.example.demo.hotelink.service; // Pon tu paquete correcto

import com.example.demo.hotelink.model.Habitacion;
import com.example.demo.hotelink.model.TareaLimpieza;
import com.example.demo.hotelink.repository.TareaLimpiezaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TareaLimpiezaService {

    @Autowired
    private TareaLimpiezaRepository tareaRepository;

    public List<TareaLimpieza> obtenerTodas() {
        return tareaRepository.findAll();
    }

    // El método que llama el ReservaService al hacer Check-Out
    public void crearTareaParaHabitacion(Habitacion habitacion) {
        TareaLimpieza nuevaTarea = new TareaLimpieza();
        
        nuevaTarea.setHabitacion(habitacion);
        nuevaTarea.setFecha(LocalDate.now());
        nuevaTarea.setEstado("PENDIENTE");
        
        tareaRepository.save(nuevaTarea);
    }
}