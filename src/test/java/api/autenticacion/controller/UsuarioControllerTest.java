package api.autenticacion.controller;

import api.autenticacion.model.Rol;
import api.autenticacion.model.Usuario;
import api.autenticacion.model.UsuarioRequest;
import api.autenticacion.repository.RolRepository;
import api.autenticacion.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private RolRepository rolRepository;

    private Usuario usuarioMock;
    private UsuarioRequest requestMock;
    private Rol rolMock;

    @BeforeEach
    void setup() {
        // Inicializa el controlador e inyecta los mocks manualmente
        UsuarioController usuarioController = new UsuarioController();
        ReflectionTestUtils.setField(usuarioController, "usuarioService", usuarioService);
        ReflectionTestUtils.setField(usuarioController, "rolRepository", rolRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();

        rolMock = new Rol(1L, 1, "ROLE_ADMIN", "Admin");
        usuarioMock = new Usuario(1L, rolMock, "Juan", "Perez", "juan@test.com", "password", "Dir", "123");
        
        requestMock = new UsuarioRequest();
        requestMock.setNombre("Juan");
        requestMock.setApellido("Perez");
        requestMock.setCorreo("juan@test.com");
        requestMock.setContrasena("password");
        requestMock.setDireccion("Dir");
        requestMock.setTelefono("123");
        requestMock.setNumero_rol(1);
    }

    /**
     * Prueba que GET /usuarios devuelva una lista de todos los usuarios
     * con estado HTTP 200 (OK).
     */
    @Test
    void getAllUsuarios_ReturnsList() throws Exception {
        List<Usuario> usuarios = Arrays.asList(usuarioMock);
        when(usuarioService.getAllUsuarios()).thenReturn(usuarios);

        mockMvc.perform(get("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Juan"));
    }

    /**
     * Prueba que GET /usuarios/{id} devuelva el usuario correcto
     * si existe, con estado HTTP 200 (OK).
     */
    @Test
    void getUsuarioById_UserExists_ReturnsOk() throws Exception {
        when(usuarioService.getUsuarioById(1L)).thenReturn(Optional.of(usuarioMock));

        mockMvc.perform(get("/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("juan@test.com"));
    }

    /**
     * Prueba que GET /usuarios/{id} devuelva estado HTTP 404 (Not Found)
     * si el usuario no existe.
     */
    @Test
    void getUsuarioById_UserDoesNotExist_ReturnsNotFound() throws Exception {
        when(usuarioService.getUsuarioById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/usuarios/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Prueba que POST /usuarios cree un usuario de forma exitosa
     * cuando se envía un request válido y un rol existente.
     */
    @Test
    void createUsuario_ValidRequest_ReturnsOk() throws Exception {
        when(rolRepository.findByNumeroRol(1)).thenReturn(Optional.of(rolMock));
        when(usuarioService.saveUsuario(any(Usuario.class))).thenReturn(usuarioMock);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    /**
     * Prueba que POST /usuarios lanze una excepción en el servidor
     * si se intenta crear un usuario con un número de rol inexistente.
     */
    @Test
    void createUsuario_InvalidRol_ThrowsException() throws Exception {
        when(rolRepository.findByNumeroRol(1)).thenReturn(Optional.empty());

        try {
            mockMvc.perform(post("/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestMock)));
        } catch (Exception e) {
            assert e.getCause().getMessage().contains("Rol no encontrado");
        }
    }

    /**
     * Prueba que PUT /usuarios/{id} actualice la información de un usuario
     * y responda con HTTP 200 (OK).
     */
    @Test
    void updateUsuario_ReturnsOk() throws Exception {
        when(rolRepository.findByNumeroRol(1)).thenReturn(Optional.of(rolMock));
        when(usuarioService.updateUsuario(eq(1L), any(Usuario.class))).thenReturn(usuarioMock);

        mockMvc.perform(put("/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    /**
     * Prueba que DELETE /usuarios/{id} borre correctamente al usuario
     * y responda con HTTP 204 (No Content).
     */
    @Test
    void deleteUsuario_ReturnsNoContent() throws Exception {
        doNothing().when(usuarioService).deleteUsuario(1L);

        mockMvc.perform(delete("/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).deleteUsuario(1L);
    }

    /**
     * Prueba que PATCH /usuarios/{id}/change-password modifique la contraseña
     * y devuelva un HTTP 200 (OK).
     */
    @Test
    void changePassword_ReturnsOk() throws Exception {
        Map<String, String> request = Map.of("newPassword", "newPass");
        when(usuarioService.cambioContrasena(1L, "newPass")).thenReturn(usuarioMock);

        mockMvc.perform(patch("/usuarios/1/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
