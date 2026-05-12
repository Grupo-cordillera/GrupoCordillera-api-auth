package api.autenticacion.controller;

import api.autenticacion.model.Rol;
import api.autenticacion.service.RolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RolControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RolService rolService;

    private Rol rolMock;

    @BeforeEach
    void setup() {
        // Inicializa el controlador inyectando el servicio mockeado
        RolController rolController = new RolController(rolService);
        mockMvc = MockMvcBuilders.standaloneSetup(rolController).build();

        rolMock = new Rol(1L, 1, "ROLE_ADMIN", "Admin");
    }

    /**
     * Prueba que el endpoint GET /api/rol devuelva la lista de roles correctamente
     * con un estado HTTP 200 (OK).
     */
    @Test
    void obtenerRoles_ReturnsOkWithList() throws Exception {
        List<Rol> roles = Arrays.asList(rolMock, new Rol(2L, 2, "ROLE_USER", "User"));
        when(rolService.obtenerTodosLosRoles()).thenReturn(roles);

        mockMvc.perform(get("/api/rol")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$[1].nombre").value("ROLE_USER"));
    }

    /**
     * Prueba que el endpoint GET /api/rol/{id} devuelva un rol específico
     * cuando este existe en la base de datos, con estado HTTP 200 (OK).
     */
    @Test
    void obtenerRolPorId_RolExists_ReturnsOk() throws Exception {
        when(rolService.obtenerRolPorId(1L)).thenReturn(Optional.of(rolMock));

        mockMvc.perform(get("/api/rol/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ROLE_ADMIN"));
    }

    /**
     * Prueba que el endpoint GET /api/rol/{id} devuelva un estado HTTP 404 (Not Found)
     * cuando se solicita un ID que no existe.
     */
    @Test
    void obtenerRolPorId_RolDoesNotExist_ReturnsNotFound() throws Exception {
        when(rolService.obtenerRolPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/rol/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Prueba que el endpoint POST /api/rol cree un nuevo rol y devuelva
     * el rol creado junto con un estado HTTP 201 (Created).
     */
    @Test
    void crearRol_ReturnsCreated() throws Exception {
        when(rolService.guardarRol(any(Rol.class))).thenReturn(rolMock);

        mockMvc.perform(post("/api/rol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolMock)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("ROLE_ADMIN"));
    }

    /**
     * Prueba que el endpoint DELETE /api/rol/{id} elimine un rol
     * y devuelva un estado HTTP 204 (No Content).
     */
    @Test
    void eliminarRol_ReturnsNoContent() throws Exception {
        doNothing().when(rolService).eliminarRol(1L);

        mockMvc.perform(delete("/api/rol/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verifica que el servicio de eliminar fue llamado exactamente 1 vez
        verify(rolService, times(1)).eliminarRol(1L);
    }
}
