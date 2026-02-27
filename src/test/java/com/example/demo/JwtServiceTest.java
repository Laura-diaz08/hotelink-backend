package com.example.demo;

import com.example.demo.hotelink.auth.JwtService;
import com.example.demo.hotelink.model.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    // Instanciamos el servicio que vamos a probar
    private JwtService jwtService;

    // @BeforeEach hace que este método se ejecute ANTES de cada prueba
    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    // PRUEBA 1: Verificar que se genera un token y no está vacío
    @Test
    void generarToken_DeberiaRetornarTokenNoNulo() {
        // Ejecutamos el método
        String token = jwtService.generarToken("usuarioPrueba", Rol.USER);

        // Verificamos (Afirmamos) que el token no sea nulo ni esté en blanco
        assertNotNull(token, "El token generado no debería ser nulo");
        assertFalse(token.isEmpty(), "El token generado no debería estar vacío");
        
        // Un token JWT siempre tiene 3 partes separadas por dos puntos (.)
        String[] partes = token.split("\\.");
        assertEquals(3, partes.length, "El token JWT debe tener exactamente 3 partes (Header.Payload.Signature)");
    }

    // PRUEBA 2: Verificar que podemos extraer el nombre exacto que guardamos
    @Test
    void obtenerNombre_DeberiaRetornarNombreCorrecto() {
        // Preparamos los datos
        String nombreOriginal = "adminHotelink";
        String token = jwtService.generarToken(nombreOriginal, Rol.ADMIN);

        // Extraemos el nombre usando tu método
        String nombreExtraido = jwtService.obtenerNombre(token);

        // Verificamos que el nombre extraído sea exactamente igual al original
        assertEquals(nombreOriginal, nombreExtraido, "El nombre extraído debe coincidir con el original");
    }

    // PRUEBA 3: Verificar que la seguridad funciona y rechaza tokens falsos
    @Test
    void tokenValido_DeberiaRetornarFalsoParaTokenInventado() {
        // Nos inventamos un token falso (o uno que haya sido manipulado por un hacker)
        String tokenFalso = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.falsoPayload.falsaFirma";

        // Ejecutamos tu método de validación
        boolean esValido = jwtService.tokenValido(tokenFalso);

        // Verificamos que el sistema diga que es FALSO
        assertFalse(esValido, "El sistema debe rechazar un token que no haya sido firmado por nosotros");
    }
}
