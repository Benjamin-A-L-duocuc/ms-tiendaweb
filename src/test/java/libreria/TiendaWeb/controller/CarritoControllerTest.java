package libreria.TiendaWeb.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import libreria.TiendaWeb.model.Carrito;
import libreria.TiendaWeb.model.enums.EstadoCarrito;
import libreria.TiendaWeb.service.CarritoService;

@WebMvcTest(CarritoController.class)
class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private CarritoService carritoService;

    @Test
    void verCatalogo_sinBusqueda_retorna200() throws Exception {
        List<Map<String, Object>> productos = List.of(
            Map.of("id", 1L, "nombre", "Libro A"),
            Map.of("id", 2L, "nombre", "Libro B")
        );
        when(carritoService.verCatalogo(isNull(), isNull(), eq(true))).thenReturn(productos);

        mockMvc.perform(get("/api/v1/tienda/catalogo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nombre").value("Libro A"))
            .andExpect(jsonPath("$[1].nombre").value("Libro B"));
    }

    @Test
    void verCatalogo_conBusqueda_retorna200() throws Exception {
        List<Map<String, Object>> productos = List.of(
            Map.of("id", 1L, "nombre", "Java Programming")
        );
        when(carritoService.verCatalogo(eq("java"), isNull(), eq(true))).thenReturn(productos);

        mockMvc.perform(get("/api/v1/tienda/catalogo").param("busqueda", "java"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nombre").value("Java Programming"));
    }

    @Test
    void verDetalleProducto_encontrado_retorna200() throws Exception {
        Map<String, Object> producto = Map.of("id", 1L, "nombre", "Libro X", "stockTotal", 10);
        when(carritoService.verDetalleProducto(1L)).thenReturn(producto);

        mockMvc.perform(get("/api/v1/tienda/productos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Libro X"))
            .andExpect(jsonPath("$.stockTotal").value(10));
    }

    @Test
    void verDetalleProducto_noEncontrado_retorna404() throws Exception {
        when(carritoService.verDetalleProducto(99L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/tienda/productos/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void agregarAlCarrito_exito_retorna200() throws Exception {
        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null);
        when(carritoService.agregarAlCarrito(1L, 100L, 2)).thenReturn(carrito);

        mockMvc.perform(post("/api/v1/tienda/carrito/agregar")
                .param("idSesion", "1")
                .param("idProducto", "100")
                .param("cantidad", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.estado").value("activo"));
    }

    @Test
    void agregarAlCarrito_error_retorna400() throws Exception {
        when(carritoService.agregarAlCarrito(eq(1L), eq(100L), eq(2)))
            .thenThrow(new RuntimeException("Stock insuficiente"));

        mockMvc.perform(post("/api/v1/tienda/carrito/agregar")
                .param("idSesion", "1")
                .param("idProducto", "100")
                .param("cantidad", "2"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Stock insuficiente"));
    }

    @Test
    void modificarItemCarrito_exito_retorna200() throws Exception {
        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null);
        when(carritoService.modificarItemCarrito(1L, 100L, 3)).thenReturn(carrito);

        mockMvc.perform(put("/api/v1/tienda/carrito/modificar")
                .param("idSesion", "1")
                .param("idProducto", "100")
                .param("cantidad", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void modificarItemCarrito_error_retorna400() throws Exception {
        when(carritoService.modificarItemCarrito(eq(1L), eq(100L), eq(3)))
            .thenThrow(new RuntimeException("Cantidad debe ser mayor a 0"));

        mockMvc.perform(put("/api/v1/tienda/carrito/modificar")
                .param("idSesion", "1")
                .param("idProducto", "100")
                .param("cantidad", "3"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Cantidad debe ser mayor a 0"));
    }

    @Test
    void eliminarItemCarrito_exito_retorna204() throws Exception {
        mockMvc.perform(delete("/api/v1/tienda/carrito/items/1")
                .param("idSesion", "1"))
            .andExpect(status().isNoContent());

        verify(carritoService).eliminarItemCarrito(1L, 1L);
    }

    @Test
    void eliminarItemCarrito_error_retorna400() throws Exception {
        doThrow(new RuntimeException("Item no encontrado"))
            .when(carritoService).eliminarItemCarrito(1L, 99L);

        mockMvc.perform(delete("/api/v1/tienda/carrito/items/99")
                .param("idSesion", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Item no encontrado"));
    }

    @Test
    void revisarCarrito_exito_retorna200() throws Exception {
        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null);
        when(carritoService.revisarCarrito(1L)).thenReturn(carrito);

        mockMvc.perform(get("/api/v1/tienda/carrito")
                .param("idSesion", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void revisarCarrito_error_retorna400() throws Exception {
        when(carritoService.revisarCarrito(1L))
            .thenThrow(new RuntimeException("Carrito no encontrado"));

        mockMvc.perform(get("/api/v1/tienda/carrito")
                .param("idSesion", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Carrito no encontrado"));
    }

    @Test
    void crearCarrito_retorna200() throws Exception {
        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null);
        when(carritoService.guardar(any(Carrito.class))).thenReturn(carrito);

        mockMvc.perform(post("/api/v1/tienda/carrito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Carrito())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerTodos_retorna200() throws Exception {
        when(carritoService.obtenerTodos()).thenReturn(List.of(
            new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null)
        ));

        mockMvc.perform(get("/api/v1/tienda/carritos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void obtenerPorId_encontrado_retorna200() throws Exception {
        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null);
        when(carritoService.obtenerPorId(1L)).thenReturn(Optional.of(carrito));

        mockMvc.perform(get("/api/v1/tienda/carritos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerPorId_noEncontrado_retorna404() throws Exception {
        when(carritoService.obtenerPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/tienda/carritos/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void actualizar_exito_retorna200() throws Exception {
        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null);
        when(carritoService.actualizar(eq(1L), any(Carrito.class))).thenReturn(carrito);

        mockMvc.perform(put("/api/v1/tienda/carritos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Carrito())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void actualizar_noEncontrado_retorna404() throws Exception {
        when(carritoService.actualizar(eq(99L), any(Carrito.class)))
            .thenThrow(new RuntimeException("Carrito no encontrado"));

        mockMvc.perform(put("/api/v1/tienda/carritos/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Carrito())))
            .andExpect(status().isNotFound());
    }

    @Test
    void eliminar_retorna204() throws Exception {
        mockMvc.perform(delete("/api/v1/tienda/carritos/1"))
            .andExpect(status().isNoContent());

        verify(carritoService).eliminar(1L);
    }
}
