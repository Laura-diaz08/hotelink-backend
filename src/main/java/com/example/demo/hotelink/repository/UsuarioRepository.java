package com.example.demo.hotelink.repository;

import com.example.demo.hotelink.model.Rol;
import com.example.demo.hotelink.model.Usuario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByNombre(String nombre);
    List<Usuario> findByRol(Rol rol);
}


