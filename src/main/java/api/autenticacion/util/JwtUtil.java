package api.autenticacion.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys; // Importante: importar Keys
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey; // Importante: importar SecretKey
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // SonarQube fix: Usar camelCase para variables de instancia
    @Value("${jwt.secret.key:este-es-un-secreto-por-defecto-muy-largo-1234567890}")
    private String secretKey;

    // --- MÉTODOS DE EXTRACCIÓN (MODIFICADOS PARA USAR EL NUEVO PARSER) ---

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * CORREGIDO: Ahora usa el parserBuilder() moderno y seguro.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // --- MÉTODOS DE GENERACIÓN Y VALIDACIÓN (MODIFICADOS) ---

    // SonarQube fix: Usar boolean primitivo en vez de Boolean objeto
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Añadir el rol al token
        userDetails.getAuthorities().stream()
                .findFirst() // Como el usuario tiene un solo rol, tomamos el primero.
                .ifPresent(authority -> claims.put("rol", authority.getAuthority()));
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * CORREGIDO: Ahora usa el método signWith que recibe un objeto SecretKey.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 horas
                .signWith(getSigningKey()) // ¡El método moderno!
                .compact();
    }

    // SonarQube fix: Usar boolean primitivo
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * NUEVO MÉTODO AUXILIAR:
     * Convierte la clave secreta (String) en un objeto SecretKey, que es lo que
     * la nueva versión de la librería necesita para firmar y verificar de forma segura.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = this.secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
