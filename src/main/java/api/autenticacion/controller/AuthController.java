package api.autenticacion.controller;

import api.autenticacion.model.AuthenticationRequest;
import api.autenticacion.model.AuthenticationResponse;
import api.autenticacion.model.Usuario;
import api.autenticacion.repository.UsuarioRepository;
import api.autenticacion.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            // SonarQube: No lanzar java.lang.Exception genérica.
            // Lanzamos BadCredentialsException que ya es manejada por Spring o por un @ExceptionHandler.
            throw new BadCredentialsException("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);
        
        // Buscar el usuario en la base de datos para obtener los datos extra
        Usuario usuario = usuarioRepository.findByCorreo(authenticationRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado en la BD"));
                
        // Concatenar nombre y apellido
        String nombreCompleto = usuario.getNombre() + " " + usuario.getApellido();

        // Devolver la respuesta con todos los datos. 
        // SonarQube: ResponseEntity<AuthenticationResponse> en vez de ResponseEntity<?>
        return ResponseEntity.ok(new AuthenticationResponse(
                jwt,
                nombreCompleto,
                usuario.getCorreo(),
                usuario.getDireccion(),
                usuario.getTelefono(),
                usuario.getRol().getNombre()
        ));
    }
}
