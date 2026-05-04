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

import com.example.demo.hotelink.model.Reserva;
import com.example.demo.hotelink.repository.ReservaRepository;
import com.example.demo.hotelink.service.ReservaService;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository repo;

    @InjectMocks
    private ReservaService reservaService;

    @Test
    void cancelarReserva_ReservaNoEncontrada() {

        Long reservaId = 1L;
        when(repo.findById(reservaId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = reservaService.cancelarReserva(reservaId);

        assertEquals(404, response.getStatusCode().value());
        verify(repo, never()).save(any());
    }

    @Test
    void cancelarReserva_ReservaExistente_DeberiaActualizarEstadoYRetornarOk() {

        Long reservaId = 1L;
        Reserva reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setEstado("CONFIRMADA");

        when(repo.findById(reservaId)).thenReturn(Optional.of(reserva));

        ResponseEntity<?> response = reservaService.cancelarReserva(reservaId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("CANCELADA", reserva.getEstado());
        verify(repo, times(1)).save(any(Reserva.class));
    }
}