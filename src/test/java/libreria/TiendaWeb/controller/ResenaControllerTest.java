package libreria.TiendaWeb.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import libreria.TiendaWeb.model.Resena;
import libreria.TiendaWeb.service.ResenaService;

@WebMvcTest(ResenaController.class)
class ResenaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ResenaService resenaService;

    @Test
    void postResenas_retorna200() throws Exception {
        Resena resena = new Resena(1L, 10L, 100L, 5, "Excelente libro, muy recomendado", null);
        when(resenaService.reseñarProducto(any(), eq(1L))).thenReturn(resena);

        mockMvc.perform(post("/api/v1/tienda/resenas")
                .param("idSesion", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resena)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void postResenas_error_retorna400() throws Exception {
        when(resenaService.reseñarProducto(any(), eq(1L)))
            .thenThrow(new RuntimeException("Sesion no valida"));

        Resena resena = new Resena(null, null, 100L, 5, "Excelente libro", null);

        mockMvc.perform(post("/api/v1/tienda/resenas")
                .param("idSesion", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resena)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Sesion no valida"));
    }

    @Test
    void putResenas_retorna200() throws Exception {
        Resena resena = new Resena(1L, 10L, 100L, 5, "Comentario actualizado y mejorado", null);
        when(resenaService.cambiarResena(any(), eq(1L))).thenReturn(resena);

        mockMvc.perform(put("/api/v1/tienda/resenas")
                .param("idSesion", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resena)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void putResenas_error_retorna400() throws Exception {
        when(resenaService.cambiarResena(any(), eq(1L)))
            .thenThrow(new RuntimeException("Resena no encontrada"));

        Resena resena = new Resena();

        mockMvc.perform(put("/api/v1/tienda/resenas")
                .param("idSesion", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resena)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Resena no encontrada"));
    }

    @Test
    void getResenasPorProducto_retorna200() throws Exception {
        when(resenaService.obtenerResenasPorProducto(100L))
            .thenReturn(List.of(new Resena(1L, 10L, 100L, 5, "Buen libro", null)));

        mockMvc.perform(get("/api/v1/tienda/productos/100/resenas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getResenas_retorna200() throws Exception {
        when(resenaService.obtenerTodas())
            .thenReturn(List.of(new Resena(1L, 10L, 100L, 4, "Resena de prueba", null)));

        mockMvc.perform(get("/api/v1/tienda/resenas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getResenasPorId_retorna200() throws Exception {
        when(resenaService.obtenerPorId(1L))
            .thenReturn(Optional.of(new Resena(1L, 10L, 100L, 5, "Resena individual", null)));

        mockMvc.perform(get("/api/v1/tienda/resenas/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getResenasPorId_noEncontrado_retorna404() throws Exception {
        when(resenaService.obtenerPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/tienda/resenas/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteResenas_retorna204() throws Exception {
        mockMvc.perform(delete("/api/v1/tienda/resenas/1"))
            .andExpect(status().isNoContent());

        verify(resenaService).eliminar(1L);
    }
}
