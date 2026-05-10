package api.autenticacion.service;

import api.autenticacion.model.Rol;
import api.autenticacion.model.Usuario;
import api.autenticacion.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private Rol rol;

    @BeforeEach
    void setUp() {
        rol = new Rol(1L, 1, "ADMIN", "Administrador");
        
        // Se instancia usando el constructor AllArgsConstructor u setters normales
        // para no depender de @Builder que fue removido del modelo base.
        usuario = new Usuario(
                1L, 
                rol, 
                "Juan", 
                "Perez", 
                "juan@test.com", 
                "password123", 
                "Calle Falsa 123", 
                "123456789"
        );
    }

    @Test
    void loadUserByUsername_UserExists_ReturnsUserDetails() {
        when(usuarioRepository.findByCorreo(anyString())).thenReturn(Optional.of(usuario));

        UserDetails userDetails = usuarioService.loadUserByUsername("juan@test.com");

        assertNotNull(userDetails);
        assertEquals("juan@test.com", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        verify(usuarioRepository, times(1)).findByCorreo("juan@test.com");
    }

    @Test
    void loadUserByUsername_UserDoesNotExist_ThrowsUsernameNotFoundException() {
        when(usuarioRepository.findByCorreo(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            usuarioService.loadUserByUsername("notfound@test.com");
        });
        verify(usuarioRepository, times(1)).findByCorreo("notfound@test.com");
    }

    @Test
    void getAllUsuarios_ReturnsListOfUsuarios() {
        Usuario usuario2 = new Usuario();
        usuario2.setId(2L);
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario, usuario2));

        List<Usuario> usuarios = usuarioService.getAllUsuarios();

        assertNotNull(usuarios);
        assertEquals(2, usuarios.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    void getUsuarioById_UserExists_ReturnsUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Optional<Usuario> result = usuarioService.getUsuarioById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Juan", result.get().getNombre());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    void saveUsuario_EncodesPasswordAndSavesUser() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario savedUser = usuarioService.saveUsuario(usuario);

        assertNotNull(savedUser);
        assertEquals("encodedPassword", usuario.getContrasena());
        verify(passwordEncoder, times(1)).encode("password123");
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void deleteUsuario_CallsRepositoryDelete() {
        doNothing().when(usuarioRepository).deleteById(1L);

        usuarioService.deleteUsuario(1L);

        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    void updateUsuario_UserExists_UpdatesAndSaves() {
        Usuario updatedDetails = new Usuario();
        updatedDetails.setCorreo("nuevo@test.com");
        updatedDetails.setDireccion("Nueva Direccion");
        updatedDetails.setTelefono("987654321");
        updatedDetails.setRol(rol);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario result = usuarioService.updateUsuario(1L, updatedDetails);

        assertNotNull(result);
        assertEquals("nuevo@test.com", result.getCorreo());
        assertEquals("Nueva Direccion", result.getDireccion());
        assertEquals("987654321", result.getTelefono());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void updateUsuario_UserDoesNotExist_ThrowsException() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.updateUsuario(1L, new Usuario());
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambioContrasena_UserExists_EncodesNewPasswordAndSaves() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario result = usuarioService.cambioContrasena(1L, "newPassword");

        assertNotNull(result);
        assertEquals("newEncodedPassword", result.getContrasena());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(usuarioRepository, times(1)).save(usuario);
    }
}
