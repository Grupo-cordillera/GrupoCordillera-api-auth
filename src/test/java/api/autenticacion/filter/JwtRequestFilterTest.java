package api.autenticacion.filter;

import api.autenticacion.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtRequestFilterTest {

    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Limpiamos el contexto de seguridad antes de cada prueba
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Limpiamos el contexto después de cada prueba para evitar interferencias
        SecurityContextHolder.clearContext();
    }

    /**
     * Prueba el flujo correcto: El usuario envía un token JWT válido en el header,
     * el filtro lo lee, lo valida, carga los datos del usuario y lo inyecta
     * en el contexto de seguridad (logueándolo).
     */
    @Test
    void doFilterInternal_ValidToken_AuthenticatesUser() throws Exception {
        String token = "valid-token";
        String username = "testuser";

        // Simulamos la cabecera con el token
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);

        jwtRequestFilter.doFilterInternal(request, response, chain);

        // Verificamos que el usuario quedó autenticado en Spring Security
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        
        // Verificamos que se permitió continuar la petición (chain.doFilter)
        verify(chain, times(1)).doFilter(request, response);
    }

    /**
     * Prueba el flujo anónimo: El usuario hace una petición sin enviar
     * la cabecera Authorization. El filtro debe dejar pasar la petición
     * pero NO debe autenticar a nadie.
     */
    @Test
    void doFilterInternal_NoHeader_DoesNotAuthenticate() throws Exception {
        // Simulamos que no hay cabecera
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtRequestFilter.doFilterInternal(request, response, chain);

        // Verificamos que no hay nadie autenticado
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain, times(1)).doFilter(request, response);
    }

    /**
     * Prueba el flujo de error: El usuario envía un token falso o expirado.
     * El filtro intenta validarlo, falla, y por ende, deja pasar la petición
     * SIN autenticar a nadie (luego Spring Security lo bloqueará con un 403).
     */
    @Test
    void doFilterInternal_InvalidToken_DoesNotAuthenticate() throws Exception {
        String token = "invalid-token";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        
        // Simulamos que el token no pasó la validación
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(false);

        jwtRequestFilter.doFilterInternal(request, response, chain);

        // Verificamos que no hay nadie autenticado
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain, times(1)).doFilter(request, response);
    }
}
