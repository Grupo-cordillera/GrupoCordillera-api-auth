package api.autenticacion.model;

import lombok.Data;

@Data
public class UsuarioRequest {
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasena;
    private String direccion;
    private String telefono;
    // SonarQube fix: Renombrado de numero_rol a numeroRol (camelCase)
    private Integer numeroRol;
}
