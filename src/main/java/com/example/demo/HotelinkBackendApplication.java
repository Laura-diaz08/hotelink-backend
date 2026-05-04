package com.example.demo;

import com.example.demo.hotelink.model.Rol;
import com.example.demo.hotelink.model.Usuario;
import com.example.demo.hotelink.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication; 
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder; 

@SpringBootApplication
@EnableScheduling
public class HotelinkBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelinkBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UsuarioRepository repo, PasswordEncoder passwordEncoder) {
        return args -> {
            if (repo.count() == 0) {
                Usuario admin = new Usuario();
                admin.setNombre("admin");
                
                // Encriptamos la contraseña
                admin.setPassword(passwordEncoder.encode("1234")); 
                
                admin.setEmail("admin@hotel.com");
                admin.setRol(Rol.ADMIN);
                
                repo.save(admin);
                
                System.out.println("--------------------------------");
                System.out.println("¡USUARIO DIRECTOR CREADO!");
                System.out.println("Usuario: admin | Pass: 1234 (Encriptada en BD)");
                System.out.println("--------------------------------");
            } else {
                System.out.println("La base de datos ya contiene usuarios, no se ha creado el admin automático.");
            }
        };
    }
}