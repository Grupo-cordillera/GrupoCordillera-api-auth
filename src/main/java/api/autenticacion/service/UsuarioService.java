package api.autenticacion.service;

import api.autenticacion.model.Usuario;
import api.autenticacion.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario saveUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    public Usuario updateUsuario(Long id, Usuario usuarioDetails) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setRol(usuarioDetails.getRol());
        usuario.setCorreo(usuarioDetails.getCorreo());
        usuario.setDireccion(usuarioDetails.getDireccion());
        usuario.setTelefono(usuarioDetails.getTelefono());
        return usuarioRepository.save(usuario);
    }

    public Usuario cambioContrasena(Long id, String newPassword) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setContrasena(newPassword);
        return usuarioRepository.save(usuario);
    }
}
