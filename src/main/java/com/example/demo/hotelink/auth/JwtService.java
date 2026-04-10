package com.example.demo.hotelink.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.example.demo.hotelink.model.Rol;

@Service
public class JwtService {

    private final Key secretKey = Keys.hmacShaKeyFor(
            "CLAVE_SUPER_SECRETA_PARA_MI_PROYECTO_123".getBytes()
    );

    private final long expirationMs = 3600000; 

    // Crear token
    public String generarToken(String nombre, Rol rol) {

        return Jwts.builder()
                .setSubject(nombre)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    //Validar token
    public boolean tokenValido(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true; // token correcto
        } catch (Exception e) {
            return false; // token inválido o expirado
        }
    }

    //Obtener el nombre del token
    public String obtenerNombre(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    // Obtener rol del token
    public Rol obtenerRol(String token) {
        try {
            // 1. Lo extraemos como String explícitamente
            String rolTexto = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("rol", String.class);
            
            // 2. Convertimos el String a tu Enum Rol
            return Rol.valueOf(rolTexto);
            
        } catch (Exception e) {
            System.out.println("Error al obtener el rol: " + e.getMessage());
            return null;
        }
    }

    // Verifica si es un admin
    public boolean adminValido(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Fallo 1: El header es nulo o no empieza por Bearer");
            return false;
        }

        String token = authHeader.substring(7);
        String usuario = obtenerNombre(token);
        
        if (usuario == null) {
            System.out.println("Fallo 2: No se pudo obtener el usuario del token");
            return false;
        }

        Rol rol = obtenerRol(token);
        System.out.println("El usuario es: " + usuario + " y su rol en el token es: " + rol);

        return Rol.ADMIN.equals(rol);
    }

    //Validar que un token sea de cualquier usuario 
    public boolean usuarioValido(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) 
            return false;

        String token = authHeader.substring(7);
        //Valido solo si el token es correcto y tiene almacenado un usuario
        return tokenValido(token) && obtenerNombre(token) != null;
    }
}
