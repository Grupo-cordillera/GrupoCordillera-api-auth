package api.autenticacion.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDto {
    private Long id;
    private RolDto rol;
    private String nombre;
    private String apellido;
    private String correo;
    private String direccion;
    private String telefono;
}
