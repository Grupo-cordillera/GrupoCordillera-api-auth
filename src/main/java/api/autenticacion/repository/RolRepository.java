package api.autenticacion.repository;

import api.autenticacion.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    // Método para buscar un rol por su nombre
    Optional<Rol> findByNombre(String nombre);

    // Método para buscar un rol por su número
    Optional<Rol> findByNumeroRol(Integer numeroRol);
}
