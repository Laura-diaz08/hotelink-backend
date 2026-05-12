package com.example.demo.hotelink.repository;

import com.example.demo.hotelink.model.Rol;
import com.example.demo.hotelink.model.Usuario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional <Usuario> findByNombre(String nombre);
    List<Usuario> findByRol(Rol rol);

    Optional<Usuario> findByEmail(String email);
    
}


