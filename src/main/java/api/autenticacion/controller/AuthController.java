package api.autenticacion.controller;

import api.autenticacion.model.AuthenticationRequest;
import api.autenticacion.model.AuthenticationResponse;
import api.autenticacion.model.Usuario;
import api.autenticacion.repository.UsuarioRepository;
import api.autenticacion.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);
        
        // Buscar el usuario en la base de datos para obtener los datos extra
        Usuario usuario = usuarioRepository.findByCorreo(authenticationRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado en la BD"));
                
        // Concatenar nombre y apellido
        String nombreCompleto = usuario.getNombre() + " " + usuario.getApellido();

        // Devolver la respuesta con todos los datos
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
