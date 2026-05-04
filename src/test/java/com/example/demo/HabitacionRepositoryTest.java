package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.hotelink.model.Habitacion;
import com.example.demo.hotelink.model.Rol;
import com.example.demo.hotelink.model.Usuario;
import com.example.demo.hotelink.repository.HabitacionRepository;
import com.example.demo.hotelink.repository.UsuarioRepository;
import com.example.demo.hotelink.service.HabitacionService;

import jakarta.transaction.Transactional;

@SpringBootTest 
@ActiveProfiles("test") 
@Transactional
class HabitacionRepositoryTest {

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void crearReserva_HabitacionOUsuarioNoEncontrados() {
        // ID inexistente en la base de datos
        Long habitacionId = 999L;
        Long clienteId = 999L;
        String fechaInicio = "2026-05-10";
        String fechaFin = "2026-05-15";

        ResponseEntity<?> response = habitacionService.crearReserva(habitacionId, clienteId, fechaInicio, fechaFin, 2);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void crearReserva_HabitacionNoDisponible() {

        Long habitacionId = 1L;
        Long clienteId = 1L;
        String fechaInicio = "2026-05-10";
        String fechaFin = "2026-05-15";

        ResponseEntity<?> response = habitacionService.crearReserva(habitacionId, clienteId, fechaInicio, fechaFin, 2);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void crearReserva_DatosCorrectos() {

        Usuario usuario = new Usuario();
        usuario.setNombre("Nombre de Prueba");
        usuario.setEmail("test@prueba.com");
        usuario.setPassword("password123");
        usuario.setRol(Rol.CLIENTE); 
        
        usuarioRepository.save(usuario);

        Habitacion habitacion = new Habitacion();
        habitacion.setNumero("101");
        habitacion.setCapacidad(4);
        habitacion.setEstado("LIBRE"); 
        habitacion.setPrecio(150.0);
        
        habitacionRepository.save(habitacion);

        Long habitacionId = habitacion.getId();
        Long clienteId = usuario.getId();
        String fechaInicio = "2026-05-10";
        String fechaFin = "2026-05-15";

        ResponseEntity<?> response = habitacionService.crearReserva(habitacionId, clienteId, fechaInicio, fechaFin, 2);

        assertEquals(200, response.getStatusCode().value());
    }
}
