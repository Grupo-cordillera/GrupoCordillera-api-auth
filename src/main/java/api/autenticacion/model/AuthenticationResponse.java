package api.autenticacion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticationResponse {
    private final String jwt;
    private final String nombre;
    private final String correo;
    private final String direccion;
    private final String telefono;
    private final String rol;
}
