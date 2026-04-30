package api.autenticacion.controller;

import api.autenticacion.model.Rol;
import api.autenticacion.model.Usuario;
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
    public Usuario createUsuario(@RequestBody Map<String, Object> requestBody) {
        Usuario usuario = new Usuario();
        usuario.setCorreo((String) requestBody.get("correo"));
        usuario.setContrasena((String) requestBody.get("contrasena"));
        usuario.setDireccion((String) requestBody.get("direccion"));
        usuario.setTelefono((String) requestBody.get("telefono"));

        Integer numeroRol = (Integer) requestBody.get("numero_rol");
        Rol rol = rolRepository.findByNumeroRol(numeroRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con numero_rol: " + numeroRol));
        usuario.setRol(rol);

        return usuarioService.saveUsuario(usuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Long id, @RequestBody Map<String, Object> requestBody) {
        Usuario usuarioDetails = new Usuario();
        usuarioDetails.setCorreo((String) requestBody.get("correo"));
        usuarioDetails.setDireccion((String) requestBody.get("direccion"));
        usuarioDetails.setTelefono((String) requestBody.get("telefono"));

        Integer numeroRol = (Integer) requestBody.get("numero_rol");
        Rol rol = rolRepository.findByNumeroRol(numeroRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con numero_rol: " + numeroRol));
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
