package com.example.demo.hotelink.auth;

import com.example.demo.hotelink.model.Rol;
import com.example.demo.hotelink.model.Usuario;
import com.example.demo.hotelink.service.UsuarioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    //CONTRUCTOR
    public AuthController(JwtService jwtService, UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder; 
    }

    //LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario loginReq) {

        Usuario userDB = usuarioService.buscarPorNombre(loginReq.getNombre());

        //Si el usuario no existe o la contraseña es incorrecta
        //El primer parámetro es la contraseña plana, el segundo es la encriptada
        if (userDB == null || !passwordEncoder.matches(loginReq.getPassword(), userDB.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Nombre o contraseña inválidos"));
        }

        //Genera un token JWT con el nombre y el rol del usuario
        String token = jwtService.generarToken(userDB.getNombre(), userDB.getRol());

        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "rol", userDB.getRol(),
                        "tipo", "Bearer",
                        "expiraEn", "1 hora",
                        "id", userDB.getId()
                )
        );
    }

    //REGISTRAR USUARIO
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario nuevoUsuario) {

        if (usuarioService.buscarPorNombre(nuevoUsuario.getNombre()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El usuario ya existe"));
        }

        // ENCRIPTAMOS LA CONTRASEÑA AQUÍ ANTES DE GUARDAR
        String passwordEncriptada = passwordEncoder.encode(nuevoUsuario.getPassword());
        nuevoUsuario.setPassword(passwordEncriptada);

        usuarioService.guardar(nuevoUsuario);

        return ResponseEntity.ok(Map.of("mensaje", "Usuario registrado correctamente"));
    }

    //RENOVAR TOKEN
    @GetMapping("/renovar")
    public ResponseEntity<?> renovar(@RequestHeader(name = "Authorization", required = false) String authHeader) {

        //Verifica que el header exista y empiece con "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token no enviado"));
        }

        //Extrae el token quitando la palabra "Bearer "
        String token = authHeader.substring(7);

        String nombre = jwtService.obtenerNombre(token);
        Rol rol = jwtService.obtenerRol(token);

        if (nombre == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token inválido"));
        }

        String nuevoToken = jwtService.generarToken(nombre, rol);

        return ResponseEntity.ok(Map.of("token", nuevoToken));
    }


}
