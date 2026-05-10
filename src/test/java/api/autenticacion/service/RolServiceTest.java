package api.autenticacion.service;

import api.autenticacion.model.Rol;
import api.autenticacion.repository.RolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolService rolService;

    private Rol rol;

    @BeforeEach
    void setUp() {
        rol = new Rol(1L, 1, "ADMIN", "Administrador del sistema");
    }

    @Test
    void obtenerTodosLosRoles_ReturnsListOfRoles() {
        Rol rol2 = new Rol(2L, 2, "USER", "Usuario regular");
        when(rolRepository.findAll()).thenReturn(Arrays.asList(rol, rol2));

        List<Rol> roles = rolService.obtenerTodosLosRoles();

        assertNotNull(roles);
        assertEquals(2, roles.size());
        verify(rolRepository, times(1)).findAll();
    }

    @Test
    void obtenerRolPorId_RoleExists_ReturnsRol() {
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));

        Optional<Rol> result = rolService.obtenerRolPorId(1L);

        assertTrue(result.isPresent());
        assertEquals("ADMIN", result.get().getNombre());
        verify(rolRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerRolPorId_RoleDoesNotExist_ReturnsEmptyOptional() {
        when(rolRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Rol> result = rolService.obtenerRolPorId(1L);

        assertFalse(result.isPresent());
        verify(rolRepository, times(1)).findById(1L);
    }

    @Test
    void guardarRol_SavesAndReturnsRol() {
        when(rolRepository.save(any(Rol.class))).thenReturn(rol);

        Rol savedRol = rolService.guardarRol(rol);

        assertNotNull(savedRol);
        assertEquals("ADMIN", savedRol.getNombre());
        verify(rolRepository, times(1)).save(rol);
    }

    @Test
    void eliminarRol_CallsRepositoryDelete() {
        doNothing().when(rolRepository).deleteById(1L);

        rolService.eliminarRol(1L);

        verify(rolRepository, times(1)).deleteById(1L);
    }
}
