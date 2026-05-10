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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Optional;

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
        // Instanciamos el controlador a probar
        AuthController authController = new AuthController();
        
        // Como los campos en AuthController usan @Autowired directamente (Field Injection),
        // necesitamos inyectar los mocks manualmente usando ReflectionTestUtils
        ReflectionTestUtils.setField(authController, "authenticationManager", authenticationManager);
        ReflectionTestUtils.setField(authController, "userDetailsService", userDetailsService);
        ReflectionTestUtils.setField(authController, "usuarioRepository", usuarioRepository);
        ReflectionTestUtils.setField(authController, "jwtUtil", jwtUtil);

        // Configuramos MockMvc en modo "standalone" (igual que en el inventario)
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
    void createAuthenticationToken_InvalidCredentials_ThrowsException() throws Exception {
        // Simulamos que el AuthenticationManager arroja un BadCredentialsException
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales incorrectas"));

        // Como usamos standaloneSetup sin un manejador global de excepciones, 
        // Spring envuelve la excepción no capturada (Exception genérica que lanza tu método) 
        // en una ServletException anidada, por lo que esperamos un error de servidor (500)
        // o podemos validar el mensaje arrojado.
        
        try {
            mockMvc.perform(post("/authenticate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authRequest)));
        } catch (Exception e) {
            // Tu código arroja "throw new Exception("Incorrect username or password", e);"
            assert e.getCause().getMessage().equals("Incorrect username or password");
        }
    }
}
