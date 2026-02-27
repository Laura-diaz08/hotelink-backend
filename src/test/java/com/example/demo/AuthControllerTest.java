package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test") 
@SpringBootTest 
@AutoConfigureMockMvc 
@Transactional 
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // PRUEBA 1: Verificar que un usuario que no existe es rechazado
    @Test
    void login_UsuarioNoExiste_DeberiaRetornarError() throws Exception {
        // Al estar usando H2, la base de datos está limpia y no hay ningún usuario creado
        String bodyJson = "{\n" +
                "  \"nombre\": \"hacker\",\n" +
                "  \"password\": \"12345\"\n" +
                "}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyJson))
                // Esperamos que Spring Security devuelva un error 4xx 
                .andExpect(status().is4xxClientError()); 
    }

    // PRUEBA 2: Verificar que el filtro bloquea el acceso sin Token JWT
    @Test
    void accederRutaProtegida_SinToken_DeberiaSerBloqueado() throws Exception {
        // Simulamos un intento de entrar a una ruta privada sin enviar la cabecera "Authorization"
        mockMvc.perform(get("/usuarios"))
                // Esperamos que el filtro nos corte el paso con un 403 Forbidden
                .andExpect(status().isForbidden()); 
    }
}
