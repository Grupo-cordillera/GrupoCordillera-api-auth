package api.autenticacion.controller;

import api.autenticacion.model.Rol;
import api.autenticacion.model.RolDto;
import api.autenticacion.model.Usuario;
import api.autenticacion.model.UsuarioDto;
import api.autenticacion.model.UsuarioRequest;
import api.autenticacion.repository.RolRepository;
import api.autenticacion.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UsuarioDto>> getAllUsuarios() {
        List<UsuarioDto> usuariosDto = usuarioService.getAllUsuarios().stream()
                .map(this::convertToDto)
                .toList();
        return ResponseEntity.ok(usuariosDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_EMPLEADO')")
    public ResponseEntity<UsuarioDto> getUsuarioById(@PathVariable Long id) {
        return usuarioService.getUsuarioById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UsuarioDto> createUsuario(@RequestBody UsuarioRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setCorreo(request.getCorreo());
        usuario.setContrasena(request.getContrasena());
        usuario.setDireccion(request.getDireccion());
        usuario.setTelefono(request.getTelefono());

        Rol rol = rolRepository.findByNumeroRol(request.getNumero_rol())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con numero_rol: " + request.getNumero_rol()));
        usuario.setRol(rol);

        Usuario nuevoUsuario = usuarioService.saveUsuario(usuario);
        return ResponseEntity.ok(convertToDto(nuevoUsuario));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UsuarioDto> updateUsuario(@PathVariable Long id, @RequestBody UsuarioRequest request) {
        Usuario usuarioDetails = new Usuario();
        usuarioDetails.setNombre(request.getNombre());
        usuarioDetails.setApellido(request.getApellido());
        usuarioDetails.setCorreo(request.getCorreo());
        usuarioDetails.setDireccion(request.getDireccion());
        usuarioDetails.setTelefono(request.getTelefono());

        Rol rol = rolRepository.findByNumeroRol(request.getNumero_rol())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con numero_rol: " + request.getNumero_rol()));
        usuarioDetails.setRol(rol);

        Usuario usuarioActualizado = usuarioService.updateUsuario(id, usuarioDetails);
        return ResponseEntity.ok(convertToDto(usuarioActualizado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/change-password")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_EMPLEADO')")
    public ResponseEntity<UsuarioDto> changePassword(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        String newPassword = requestBody.get("newPassword");
        Usuario usuarioActualizado = usuarioService.cambioContrasena(id, newPassword);
        return ResponseEntity.ok(convertToDto(usuarioActualizado));
    }

    // Métodos auxiliares para la conversión entre Entity y DTO
    private UsuarioDto convertToDto(Usuario usuario) {
        RolDto rolDto = null;
        if (usuario.getRol() != null) {
            rolDto = new RolDto(
                    usuario.getRol().getId(),
                    usuario.getRol().getNumeroRol(),
                    usuario.getRol().getNombre(),
                    usuario.getRol().getFuncion()
            );
        }
        return new UsuarioDto(
                usuario.getId(),
                rolDto,
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getDireccion(),
                usuario.getTelefono()
        );
    }
}
