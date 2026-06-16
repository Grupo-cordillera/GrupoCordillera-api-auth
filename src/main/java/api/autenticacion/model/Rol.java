package api.autenticacion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rol")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_rol", nullable = false, unique = true)
    private Integer numeroRol;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "funcion", length = 255)
    private String funcion;
}
