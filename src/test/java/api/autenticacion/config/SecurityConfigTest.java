package api.autenticacion.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    /**
     * Prueba que el bean PasswordEncoder generado sea del tipo BCryptPasswordEncoder.
     * Esto asegura que las contraseñas siempre se encripten de forma segura usando BCrypt.
     */
    @Test
    void passwordEncoder_ReturnsBCryptPasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }

    /**
     * Prueba que el AuthenticationManager de Spring Security se inicializa correctamente
     * desde la configuración general de autenticación.
     */
    @Test
    void authenticationManager_ReturnsManagerFromConfig() {
        AuthenticationManager expectedManager = mock(AuthenticationManager.class);
        try {
            when(authenticationConfiguration.getAuthenticationManager()).thenReturn(expectedManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        AuthenticationManager actualManager = securityConfig.authenticationManager(authenticationConfiguration);

        assertNotNull(actualManager);
    }
}
