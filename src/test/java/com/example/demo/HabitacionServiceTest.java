package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.example.demo.hotelink.model.Habitacion;
import com.example.demo.hotelink.model.Reserva;
import com.example.demo.hotelink.model.Usuario;
import com.example.demo.hotelink.repository.HabitacionRepository;
import com.example.demo.hotelink.repository.ReservaRepository;
import com.example.demo.hotelink.repository.UsuarioRepository;
import com.example.demo.hotelink.service.HabitacionService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class HabitacionServiceTest {

    @Mock
    private HabitacionRepository repo;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private HabitacionService habitacionService;

    @Test
    void crearReserva_HabitacionOUsuarioNoEncontrados() {

        Long habitacionId = 1L;
        Long clienteId = 1L;
        String fechaInicioStr = "2026-05-10";
        String fechaFinStr = "2026-05-15";

        when(repo.findById(habitacionId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = habitacionService.crearReserva(habitacionId, clienteId, fechaInicioStr, fechaFinStr, 2);

        assertEquals(400, response.getStatusCode().value());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void crearReserva_HabitacionNoDisponible() {

        Long habitacionId = 1L;
        Long clienteId = 1L;
        String fechaInicioStr = "2026-05-10";
        String fechaFinStr = "2026-05-15";

        Habitacion habitacion = new Habitacion();
        habitacion.setId(habitacionId);
        
        Usuario usuario = new Usuario();
        usuario.setId(clienteId);

        when(repo.findById(habitacionId)).thenReturn(Optional.of(habitacion));
        when(usuarioRepository.findById(clienteId)).thenReturn(Optional.of(usuario));
        when(repo.findDisponiblesPorFechas(LocalDate.parse(fechaInicioStr), LocalDate.parse(fechaFinStr)))
                .thenReturn(Collections.emptyList());

        ResponseEntity<?> response = habitacionService.crearReserva(habitacionId, clienteId, fechaInicioStr, fechaFinStr, 2);

        assertEquals(400, response.getStatusCode().value());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void crearReserva_DatosCorrectos() {

        Long habitacionId = 1L;
        Long clienteId = 1L;
        String fechaInicioStr = "2026-05-10";
        String fechaFinStr = "2026-05-15";

        Habitacion habitacion = new Habitacion();
        habitacion.setId(habitacionId);
        
        Usuario usuario = new Usuario();
        usuario.setId(clienteId);

        when(repo.findById(habitacionId)).thenReturn(Optional.of(habitacion));
        when(usuarioRepository.findById(clienteId)).thenReturn(Optional.of(usuario));
        when(repo.findDisponiblesPorFechas(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(habitacion));

        // Asignamos un ID al guardar la reserva para evitar el null
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva reserva = invocation.getArgument(0);
            reserva.setId(1L); 
            return reserva;
        });

        ResponseEntity<?> response = habitacionService.crearReserva(habitacionId, clienteId, fechaInicioStr, fechaFinStr, 2);

        assertEquals(200, response.getStatusCode().value());
        verify(reservaRepository, times(1)).save(any(Reserva.class)); 
    }
}