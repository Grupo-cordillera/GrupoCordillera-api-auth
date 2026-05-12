package api.autenticacion.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolDto {
    private Long id;
    private Integer numeroRol;
    private String nombre;
    private String funcion;
}
