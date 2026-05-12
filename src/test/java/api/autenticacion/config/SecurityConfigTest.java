package api.autenticacion.config;

import api.autenticacion.filter.JwtRequestFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private HttpSecurity httpSecurity;

    @Mock
    private JwtRequestFilter jwtRequestFilter;

    /**
     * Prueba que el bean PasswordEncoder generado sea del tipo BCryptPasswordEncoder.
     * Esto asegura que las contraseñas siempre se encripten de forma segura usando BCrypt.
     */
    @Test
    void passwordEncoder_ReturnsBCryptPasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    /**
     * Prueba que el AuthenticationManager de Spring Security se inicializa correctamente
     * desde la configuración general de autenticación.
     */
    @Test
    void authenticationManager_ReturnsManagerFromConfig() throws Exception {
        AuthenticationManager expectedManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(expectedManager);

        AuthenticationManager actualManager = securityConfig.authenticationManager(authenticationConfiguration);

        assertNotNull(actualManager);
    }
}
