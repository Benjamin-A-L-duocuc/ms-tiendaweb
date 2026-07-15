package libreria.TiendaWeb.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import libreria.TiendaWeb.model.Orden;
import libreria.TiendaWeb.model.enums.EstadoOrden;
import libreria.TiendaWeb.model.enums.MetodoContacto;
import libreria.TiendaWeb.service.OrdenService;

@WebMvcTest(OrdenController.class)
class OrdenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrdenService ordenService;

    @Test
    void crearOrden_exito_retorna200() throws Exception {
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setEstado(EstadoOrden.PENDIENTE_PAGO);
        when(ordenService.crearOrden(any(Orden.class), eq(1L))).thenReturn(orden);

        mockMvc.perform(post("/api/v1/tienda/ordenes")
                .param("idSesion", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Orden())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.estado").value("PENDIENTE_PAGO"));
    }

    @Test
    void crearOrden_error_retorna400() throws Exception {
        when(ordenService.crearOrden(any(Orden.class), eq(1L)))
            .thenThrow(new RuntimeException("Sesion no valida"));

        mockMvc.perform(post("/api/v1/tienda/ordenes")
                .param("idSesion", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Orden())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Sesion no valida"));
    }

    @Test
    void hacerVenta_exito_retorna200() throws Exception {
        Map<String, Object> result = new HashMap<>();
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setEstado(EstadoOrden.PAGADA);
        result.put("orden", orden);
        result.put("venta", Map.of("idVenta", 1L));
        when(ordenService.hacerVenta(1L, 1L, null)).thenReturn(result);

        mockMvc.perform(post("/api/v1/tienda/ordenes/1/venta")
                .param("idSesion", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.venta.idVenta").value(1));
    }

    @Test
    void hacerVenta_error_retorna400() throws Exception {
        when(ordenService.hacerVenta(eq(1L), eq(1L), any()))
            .thenThrow(new RuntimeException("Orden no encontrada"));

        mockMvc.perform(post("/api/v1/tienda/ordenes/1/venta")
                .param("idSesion", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Orden no encontrada"));
    }

    @Test
    void hacerVenta_conDescuento_retorna200() throws Exception {
        Map<String, Object> result = new HashMap<>();
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setEstado(EstadoOrden.PAGADA);
        result.put("orden", orden);
        result.put("venta", Map.of("idVenta", 2L));
        when(ordenService.hacerVenta(1L, 1L, 5L)).thenReturn(result);

        mockMvc.perform(post("/api/v1/tienda/ordenes/1/venta")
                .param("idSesion", "1")
                .param("idDescuento", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.venta.idVenta").value(2));
    }

    @Test
    void verPedidos_exito_retorna200() throws Exception {
        when(ordenService.verPedidos(1L)).thenReturn(List.of(new Orden(), new Orden()));

        mockMvc.perform(get("/api/v1/tienda/pedidos")
                .param("idSesion", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").exists())
            .andExpect(jsonPath("$[1]").exists());
    }

    @Test
    void verPedidos_error_retorna400() throws Exception {
        when(ordenService.verPedidos(1L))
            .thenThrow(new RuntimeException("Sesion no valida"));

        mockMvc.perform(get("/api/v1/tienda/pedidos")
                .param("idSesion", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Sesion no valida"));
    }

    @Test
    void verPerfil_exito_retorna200() throws Exception {
        Map<String, Object> usuario = Map.of("id", 10L, "nombre", "Juan");
        when(ordenService.verPerfil(1L)).thenReturn(usuario);

        mockMvc.perform(get("/api/v1/tienda/perfil")
                .param("idSesion", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void verPerfil_error_retorna400() throws Exception {
        when(ordenService.verPerfil(1L))
            .thenThrow(new RuntimeException("Sesion no valida"));

        mockMvc.perform(get("/api/v1/tienda/perfil")
                .param("idSesion", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Sesion no valida"));
    }

    @Test
    void contactarSoporte_telefono_retorna200() throws Exception {
        when(ordenService.contactarSoporte(MetodoContacto.Telefono))
            .thenReturn(Map.of("medio", "Telefono", "contacto", "+56 2 1234 5678"));

        mockMvc.perform(get("/api/v1/tienda/soporte")
                .param("metodo", "Telefono"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.medio").value("Telefono"))
            .andExpect(jsonPath("$.contacto").value("+56 2 1234 5678"));
    }

    @Test
    void contactarSoporte_correo_retorna200() throws Exception {
        when(ordenService.contactarSoporte(MetodoContacto.Correo))
            .thenReturn(Map.of("medio", "Correo", "contacto", "soporte@libreria.cl"));

        mockMvc.perform(get("/api/v1/tienda/soporte")
                .param("metodo", "Correo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.medio").value("Correo"))
            .andExpect(jsonPath("$.contacto").value("soporte@libreria.cl"));
    }

    @Test
    void verEnvioPedido_exito_retorna200() throws Exception {
        when(ordenService.verEnvioPedido(1L, 1L))
            .thenReturn(Map.of("idEnvio", 1L, "estado", "en_transito"));

        mockMvc.perform(get("/api/v1/tienda/ordenes/1/envio")
                .param("idSesion", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idEnvio").value(1));
    }

    @Test
    void verEnvioPedido_error_retorna400() throws Exception {
        when(ordenService.verEnvioPedido(eq(1L), eq(1L)))
            .thenThrow(new RuntimeException("La orden no ha sido enviada"));

        mockMvc.perform(get("/api/v1/tienda/ordenes/1/envio")
                .param("idSesion", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("La orden no ha sido enviada"));
    }

    @Test
    void obtenerTodas_retorna200() throws Exception {
        when(ordenService.obtenerTodas()).thenReturn(List.of(new Orden(), new Orden()));

        mockMvc.perform(get("/api/v1/tienda/ordenes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").exists())
            .andExpect(jsonPath("$[1]").exists());
    }

    @Test
    void obtenerPorId_encontrado_retorna200() throws Exception {
        Orden orden = new Orden();
        orden.setId(1L);
        when(ordenService.obtenerPorId(1L)).thenReturn(Optional.of(orden));

        mockMvc.perform(get("/api/v1/tienda/ordenes/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerPorId_noEncontrado_retorna404() throws Exception {
        when(ordenService.obtenerPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/tienda/ordenes/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void actualizar_exito_retorna200() throws Exception {
        Orden orden = new Orden();
        orden.setId(1L);
        when(ordenService.actualizar(eq(1L), any(Orden.class))).thenReturn(orden);

        mockMvc.perform(put("/api/v1/tienda/ordenes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Orden())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void actualizar_noEncontrado_retorna404() throws Exception {
        when(ordenService.actualizar(eq(99L), any(Orden.class)))
            .thenThrow(new RuntimeException("Orden no encontrada"));

        mockMvc.perform(put("/api/v1/tienda/ordenes/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Orden())))
            .andExpect(status().isNotFound());
    }

    @Test
    void eliminar_retorna204() throws Exception {
        mockMvc.perform(delete("/api/v1/tienda/ordenes/1"))
            .andExpect(status().isNoContent());

        verify(ordenService).eliminar(1L);
    }
}
