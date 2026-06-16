package api.autenticacion.controller;

import api.autenticacion.model.Rol;
import api.autenticacion.model.RolDto;
import api.autenticacion.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rol")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<RolDto>> obtenerRoles() {
        // SonarQube fix: Reemplazar Stream.collect(Collectors.toList()) con Stream.toList()
        List<RolDto> rolesDto = rolService.obtenerTodosLosRoles().stream()
                .map(this::convertToDto)
                .toList();
        return ResponseEntity.ok(rolesDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RolDto> obtenerRolPorId(@PathVariable Long id) {
        return rolService.obtenerRolPorId(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RolDto> crearRol(@RequestBody RolDto rolDto) {
        Rol rol = convertToEntity(rolDto);
        Rol nuevoRol = rolService.guardarRol(rol);
        return new ResponseEntity<>(convertToDto(nuevoRol), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> eliminarRol(@PathVariable Long id) {
        rolService.eliminarRol(id);
        return ResponseEntity.noContent().build();
    }

    // Métodos auxiliares para la conversión entre Entity y DTO
    private RolDto convertToDto(Rol rol) {
        return new RolDto(rol.getId(), rol.getNumeroRol(), rol.getNombre(), rol.getFuncion());
    }

    private Rol convertToEntity(RolDto dto) {
        Rol rol = new Rol();
        rol.setId(dto.getId());
        rol.setNumeroRol(dto.getNumeroRol());
        rol.setNombre(dto.getNombre());
        rol.setFuncion(dto.getFuncion());
        return rol;
    }
}
