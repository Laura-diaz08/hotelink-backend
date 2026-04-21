package com.example.demo.hotelink.config; 

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; 
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.hotelink.repository.UsuarioRepository;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
            // Conectamos el cors
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Desactivamos CSRF (necesario para APIs REST con JWT)
            .csrf(csrf -> csrf.disable())
            
            // Reglas de acceso
            .authorizeHttpRequests(auth -> auth
                // Dejamos pasar la petición options de Angular sin pedir token
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/tareas-limpieza/**").permitAll() 
                
                .requestMatchers("/auth/**").permitAll() // Dejamos libre /auth/login y /auth/register
                .requestMatchers("/error").permitAll()

                .requestMatchers(HttpMethod.POST, "/reservas/{id}/checkout").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                .anyRequest().authenticated()            
            )
            
            // Gestion de sesiones (Sin estado, puro JWT)
            .sessionManagement(sess -> sess
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Nuestro portero (El Filtro)
            // Le decimos que ejecute nuestro filtro antes de intentar el login por defecto de Spring
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return username -> {
            com.example.demo.hotelink.model.Usuario miUsuario = usuarioRepository.findByNombre(username); 
            
            if (miUsuario == null) {
                throw new UsernameNotFoundException("Usuario no encontrado en la base de datos");
            }

            return User.builder()
                    .username(miUsuario.getNombre())
                    .password(miUsuario.getPassword())
                    .roles(miUsuario.getRol().name())
                    .build();
        };
    }

    // Configuración específica de cors para que Angular (puerto 4200) pueda entrar
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitimos solo a tu frontend
        configuration.setAllowedOrigins(List.of("http://localhost:4200")); 
        
        // Permitimos los métodos HTTP comunes
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Permitimos todas las cabeceras (necesario para enviar el Token después)
        configuration.setAllowedHeaders(List.of("*"));
        
        // Permitimos enviar credenciales (cookies o headers de autenticación)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}