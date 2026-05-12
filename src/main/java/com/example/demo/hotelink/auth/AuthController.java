package com.example.demo.hotelink.auth;

import com.example.demo.hotelink.model.Rol;
import com.example.demo.hotelink.model.Usuario;
import com.example.demo.hotelink.repository.UsuarioRepository;
import com.example.demo.hotelink.service.EmailService;
import com.example.demo.hotelink.service.UsuarioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    //CONTRUCTOR
    public AuthController(JwtService jwtService, UsuarioService usuarioService, PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository, EmailService emailService) {
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder; 
    }

     private String generarCodigo() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
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

        if (!userDB.isVerificado()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Cuenta no verificada. Revisa tu correo."));
        }

        //Genera un token JWT con el nombre y el rol del usuario
        String token = jwtService.generarToken(userDB.getNombre(), userDB.getRol());

        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "rol", userDB.getRol(),
                        "tipo", "Bearer",
                        "expiraEn", "1 hora",
                        "id", userDB.getId(),
                        "nombre", userDB.getNombre()
                )
        );
    }

    //REGISTRAR USUARIO
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        // verificar si ya existe
        if (usuarioRepository.findByNombre(usuario.getNombre()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El usuario ya existe"));
        }

        // generar código
        String codigo = generarCodigo();
        usuario.setCodigoVerificacion(codigo);
        usuario.setVerificado(false);
        usuario.setRol(Rol.CLIENTE);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuarioRepository.save(usuario);

        // enviar correo
        try {
            emailService.enviarCodigoVerificacion(usuario.getEmail(), usuario.getNombre(), codigo);
        } catch (Exception e) {
            System.err.println("Error enviando correo: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of("mensaje", "Usuario registrado. Revisa tu correo para verificar la cuenta."));

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

     @PostMapping("/verificar")
    public ResponseEntity<?> verificar(@RequestBody Map<String, String> body) {
        String nombre = body.get("nombre");
        String codigo = body.get("codigo");

        Optional<Usuario> optUsuario = usuarioRepository.findByNombre(nombre);
        if (optUsuario.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));

        Usuario usuario = optUsuario.get();

        if (usuario.isVerificado())
            return ResponseEntity.ok(Map.of("mensaje", "La cuenta ya está verificada"));

        if (!codigo.equals(usuario.getCodigoVerificacion()))
            return ResponseEntity.badRequest().body(Map.of("error", "Código incorrecto"));

        usuario.setVerificado(true);
        usuario.setCodigoVerificacion(null);
        usuarioRepository.save(usuario);

        try {
            emailService.enviarBienvenida(usuario.getEmail(), usuario.getNombre());
        } catch (Exception e) {
            System.err.println("Error enviando correo de bienvenida: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of("mensaje", "Cuenta verificada correctamente"));
    }

    @PostMapping("/recuperar")
    public ResponseEntity<?> solicitarRecuperacion(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null)
            return ResponseEntity.badRequest().body(Map.of("error", "No existe ninguna cuenta con ese email"));

        String codigo = generarCodigo();
        usuario.setCodigoRecuperacion(codigo);
        usuarioRepository.save(usuario);

        try {
            emailService.enviarCodigoRecuperacion(email, usuario.getNombre(), codigo);
        } catch (Exception e) {
            System.err.println("Error enviando correo: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of("mensaje", "Código enviado a tu correo"));
    }

    @PostMapping("/recuperar/confirmar")
    public ResponseEntity<?> confirmarRecuperacion(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String codigo = body.get("codigo");
        String nuevaPassword = body.get("nuevaPassword");

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));

        if (!codigo.equals(usuario.getCodigoRecuperacion()))
            return ResponseEntity.badRequest().body(Map.of("error", "Código incorrecto"));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setCodigoRecuperacion(null);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente"));
    }

}
