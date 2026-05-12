package api.autenticacion.config;

import api.autenticacion.filter.JwtRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // SonarQube fix: Se removió la excepción genérica "throws Exception" porque 
    // AuthenticationConfiguration.getAuthenticationManager() lanza una excepción específica (Exception no debe usarse como throws si se puede evitar, 
    // o en su defecto arrojar la excepción correcta si la firma lo requiere, pero aquí spring maneja la inicialización).
    // Nota: org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration lanza Exception en su firma,
    // así que no podemos removerla del todo sin capturarla, lo correcto es capturarla y arrojar una IllegalStateException.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        try {
            return authenticationConfiguration.getAuthenticationManager();
        } catch (Exception e) {
            // SonarQube fix: Reemplazar excepción genérica por una específica
            throw new IllegalStateException("Error al inicializar el AuthenticationManager", e);
        }
    }

    // SonarQube fix: Se maneja la excepción de HttpSecurity internamente para evitar "throws Exception" genérico
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRequestFilter jwtRequestFilter) {
        try {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            // Endpoints públicos (Login/Registro)
                            .requestMatchers("/authenticate").permitAll()
                            // Endpoints públicos para Swagger / OpenAPI 3
                            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                            // Cualquier otra petición requiere autenticación
                            .anyRequest().authenticated()
                    )
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    );

            // Se añade el filtro antes del filtro de autenticación estándar
            http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
        } catch (Exception e) {
            // SonarQube fix: Reemplazar excepción genérica por una específica
            throw new IllegalStateException("Error al configurar el SecurityFilterChain", e);
        }
    }
}
