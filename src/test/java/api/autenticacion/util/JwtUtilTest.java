package api.autenticacion.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private UserDetails userDetails;
    private String secretKeyString = "este-es-un-secreto-muy-largo-y-seguro-para-probar-jwt-con-hmac-sha-256";

    @BeforeEach
    void setUp() {
        // Inyectamos la clave secreta en el JwtUtil usando ReflectionTestUtils
        // porque normalmente se inyecta desde application.properties con @Value
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", secretKeyString);

        userDetails = new User("testuser", "password", new ArrayList<>());
    }

    @Test
    void generateToken_ReturnsValidJwt() {
        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verificamos que el token se puede parsear con la misma clave
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("testuser", claims.getSubject());
    }

    @Test
    void extractUsername_ReturnsCorrectUsername() {
        String token = jwtUtil.generateToken(userDetails);
        String username = jwtUtil.extractUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    void extractExpiration_ReturnsCorrectExpirationDate() {
        Date now = new Date();
        String token = jwtUtil.generateToken(userDetails);
        Date expiration = jwtUtil.extractExpiration(token);

        assertTrue(expiration.after(now));
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String token = jwtUtil.generateToken(userDetails);
        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidUsername_ReturnsFalse() {
        String token = jwtUtil.generateToken(userDetails);
        UserDetails otherUserDetails = new User("otheruser", "password", new ArrayList<>());
        Boolean isValid = jwtUtil.validateToken(token, otherUserDetails);

        assertFalse(isValid);
    }

    @Test
    void validateToken_ExpiredToken_ThrowsExpiredJwtException() throws InterruptedException {
        // Generamos un token que expira casi inmediatamente
        String token = Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000)) // Hace un segundo
                .setExpiration(new Date(System.currentTimeMillis() - 500)) // Hace medio segundo
                .signWith(getSigningKey())
                .compact();

        // La validación debería fallar con una excepción porque io.jsonwebtoken lo lanza
        // cuando intenta extraer los claims de un token expirado.
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.validateToken(token, userDetails);
        });
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = this.secretKeyString.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
