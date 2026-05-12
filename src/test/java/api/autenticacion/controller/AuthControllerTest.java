package api.autenticacion.controller;

import api.autenticacion.model.AuthenticationRequest;
import api.autenticacion.model.Rol;
import api.autenticacion.model.Usuario;
import api.autenticacion.repository.UsuarioRepository;
import api.autenticacion.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtUtil jwtUtil;

    private AuthenticationRequest authRequest;
    private Usuario usuarioMock;
    private UserDetails userDetailsMock;

    @BeforeEach
    void setup() {
        // SonarQube fix: Usamos constructor injection en lugar de @Autowired (Field Injection)
        // Por lo tanto, ahora inicializamos el controlador pasándole los mocks en el constructor.
        AuthController authController = new AuthController(
                authenticationManager,
                userDetailsService,
                usuarioRepository,
                jwtUtil
        );

        // Configuramos MockMvc en modo "standalone"
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        authRequest = new AuthenticationRequest();
        authRequest.setUsername("juan@test.com");
        authRequest.setPassword("password123");

        Rol rolMock = new Rol(1L, 1, "ADMIN", "Admin");
        
        usuarioMock = new Usuario(
                1L, rolMock, "Juan", "Perez", "juan@test.com", "password123", "Calle Falsa", "12345"
        );

        userDetailsMock = new User("juan@test.com", "password123", new ArrayList<>());
    }

    @Test
    void createAuthenticationToken_ValidCredentials_ReturnsOkWithTokenAndData() throws Exception {
        // Simulamos que la autenticación fue exitosa
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("juan@test.com", "password123"));

        // Simulamos la carga del usuario y la generación del token
        when(userDetailsService.loadUserByUsername("juan@test.com")).thenReturn(userDetailsMock);
        when(jwtUtil.generateToken(userDetailsMock)).thenReturn("fake-jwt-token");
        
        // Simulamos la búsqueda en la BD para los datos extra
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuarioMock));

        // Ejecutamos la petición POST y validamos la respuesta
        mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("fake-jwt-token"))
                .andExpect(jsonPath("$.nombre").value("Juan Perez"))
                .andExpect(jsonPath("$.correo").value("juan@test.com"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void createAuthenticationToken_InvalidCredentials_ThrowsException() {
        // Simulamos que el AuthenticationManager arroja un BadCredentialsException
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales incorrectas"));

        try {
            mockMvc.perform(post("/authenticate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authRequest)));
        } catch (Exception e) {
            // SonarQube fix: Ahora el código lanza BadCredentialsException, que se anida en NestedServletException
            assertTrue(e.getCause() instanceof BadCredentialsException);
            assertEquals("Incorrect username or password", e.getCause().getMessage());
        }
    }
}
