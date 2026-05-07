package api.autenticacion.controller;

import api.autenticacion.model.Rol;
import api.autenticacion.model.Usuario;
import api.autenticacion.model.UsuarioRequest;
import api.autenticacion.repository.RolRepository;
import api.autenticacion.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolRepository rolRepository;

    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.getUsuarioById(id);
        return usuario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Usuario createUsuario(@RequestBody UsuarioRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setCorreo(request.getCorreo());
        usuario.setContrasena(request.getContrasena());
        usuario.setDireccion(request.getDireccion());
        usuario.setTelefono(request.getTelefono());

        Rol rol = rolRepository.findByNumeroRol(request.getNumero_rol())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con numero_rol: " + request.getNumero_rol()));
        usuario.setRol(rol);

        return usuarioService.saveUsuario(usuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Long id, @RequestBody UsuarioRequest request) {
        Usuario usuarioDetails = new Usuario();
        usuarioDetails.setNombre(request.getNombre());
        usuarioDetails.setApellido(request.getApellido());
        usuarioDetails.setCorreo(request.getCorreo());
        usuarioDetails.setDireccion(request.getDireccion());
        usuarioDetails.setTelefono(request.getTelefono());

        Rol rol = rolRepository.findByNumeroRol(request.getNumero_rol())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con numero_rol: " + request.getNumero_rol()));
        usuarioDetails.setRol(rol);

        return ResponseEntity.ok(usuarioService.updateUsuario(id, usuarioDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/change-password")
    public ResponseEntity<Usuario> changePassword(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        String newPassword = requestBody.get("newPassword");
        return ResponseEntity.ok(usuarioService.cambioContrasena(id, newPassword));
    }
}
