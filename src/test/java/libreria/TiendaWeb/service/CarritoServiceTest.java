package libreria.TiendaWeb.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import libreria.TiendaWeb.client.InventarioClient;
import libreria.TiendaWeb.client.LoginClient;
import libreria.TiendaWeb.model.Carrito;
import libreria.TiendaWeb.model.ItemCarrito;
import libreria.TiendaWeb.model.enums.EstadoCarrito;
import libreria.TiendaWeb.repository.CarritoRepository;
import libreria.TiendaWeb.repository.ItemCarritoRepository;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ItemCarritoRepository itemCarritoRepository;

    @Mock
    private LoginClient loginClient;

    @Mock
    private InventarioClient inventarioClient;

    @InjectMocks
    private CarritoService carritoService;

    @Test
    void agregarAlCarrito_conCarritoExistente() {
        Long idSesion = 1L;
        when(loginClient.validarSesion(idSesion)).thenReturn(true);
        when(loginClient.obtenerSesion(idSesion)).thenReturn((Map) Map.of("idUsuario", 10L));

        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null);
        when(carritoRepository.findByIdUsuarioAndEstado(10L, EstadoCarrito.activo))
            .thenReturn(Optional.of(carrito));
        when(inventarioClient.verificarStockSuficiente(100L, 2)).thenReturn(true);
        when(itemCarritoRepository.findByCarritoIdAndIdProducto(1L, 100L))
            .thenReturn(Optional.of(new ItemCarrito(1L, 100L, "Libro", 1, 15.0, carrito)));

        carritoService.agregarAlCarrito(idSesion, 100L, 2);

        verify(itemCarritoRepository).save(any());
        verify(carritoRepository).save(any());
    }

    @Test
    void agregarAlCarrito_nuevoCarrito() {
        Long idSesion = 1L;
        when(loginClient.validarSesion(idSesion)).thenReturn(true);
        when(loginClient.obtenerSesion(idSesion)).thenReturn((Map) Map.of("idUsuario", 10L));

        when(carritoRepository.findByIdUsuarioAndEstado(10L, EstadoCarrito.activo))
            .thenReturn(Optional.empty());

        Carrito nuevo = new Carrito(2L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null);
        when(carritoRepository.save(any(Carrito.class))).thenReturn(nuevo);
        when(inventarioClient.verificarStockSuficiente(100L, 2)).thenReturn(true);
        when(itemCarritoRepository.findByCarritoIdAndIdProducto(2L, 100L))
            .thenReturn(Optional.empty());
        when(inventarioClient.obtenerPrecioYDetalle(100L))
            .thenReturn(Map.of("nombre", "Nuevo Libro", "precioVenta", 25.0));

        carritoService.agregarAlCarrito(idSesion, 100L, 2);

        verify(itemCarritoRepository).save(any());
        verify(carritoRepository, times(2)).save(any());
    }

    @Test
    void agregarAlCarrito_stockInsuficiente_lanzaExcepcion() {
        Long idSesion = 1L;
        when(loginClient.validarSesion(idSesion)).thenReturn(true);
        when(loginClient.obtenerSesion(idSesion)).thenReturn((Map) Map.of("idUsuario", 10L));

        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null);
        when(carritoRepository.findByIdUsuarioAndEstado(10L, EstadoCarrito.activo))
            .thenReturn(Optional.of(carrito));
        when(inventarioClient.verificarStockSuficiente(100L, 2)).thenReturn(false);

        assertThatThrownBy(() -> carritoService.agregarAlCarrito(idSesion, 100L, 2))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void modificarItemCarrito_conExito() {
        Long idSesion = 1L;
        when(loginClient.validarSesion(idSesion)).thenReturn(true);
        when(loginClient.obtenerSesion(idSesion)).thenReturn((Map) Map.of("idUsuario", 10L));
        when(inventarioClient.verificarStockSuficiente(100L, 3)).thenReturn(true);

        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null);
        when(carritoRepository.findByIdUsuarioAndEstado(10L, EstadoCarrito.activo))
            .thenReturn(Optional.of(carrito));

        ItemCarrito item = new ItemCarrito(1L, 100L, "Libro", 1, 15.0, carrito);
        when(itemCarritoRepository.findByCarritoIdAndIdProducto(1L, 100L))
            .thenReturn(Optional.of(item));

        carritoService.modificarItemCarrito(idSesion, 100L, 3);

        assertThat(item.getCantidad()).isEqualTo(3);
        verify(itemCarritoRepository).save(item);
        verify(carritoRepository).save(carrito);
    }

    @Test
    void modificarItemCarrito_itemNoEncontrado_lanzaExcepcion() {
        Long idSesion = 1L;
        when(loginClient.validarSesion(idSesion)).thenReturn(true);
        when(loginClient.obtenerSesion(idSesion)).thenReturn((Map) Map.of("idUsuario", 10L));
        when(inventarioClient.verificarStockSuficiente(100L, 3)).thenReturn(true);

        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null);
        when(carritoRepository.findByIdUsuarioAndEstado(10L, EstadoCarrito.activo))
            .thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoIdAndIdProducto(1L, 100L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.modificarItemCarrito(idSesion, 100L, 3))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Producto no encontrado en el carrito");
    }

    @Test
    void eliminarItemCarrito_conExito() {
        Long idSesion = 1L;
        when(loginClient.validarSesion(idSesion)).thenReturn(true);

        Carrito carrito = new Carrito();
        ItemCarrito item = new ItemCarrito(1L, 100L, "Libro", 1, 15.0, carrito);
        when(itemCarritoRepository.findById(1L)).thenReturn(Optional.of(item));

        carritoService.eliminarItemCarrito(idSesion, 1L);

        verify(itemCarritoRepository).delete(item);
    }

    @Test
    void eliminarItemCarrito_itemNoEncontrado_lanzaExcepcion() {
        Long idSesion = 1L;
        when(loginClient.validarSesion(idSesion)).thenReturn(true);
        when(itemCarritoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.eliminarItemCarrito(idSesion, 99L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Item no encontrado");
    }

    @Test
    void revisarCarrito_conExito() {
        Long idSesion = 1L;
        when(loginClient.validarSesion(idSesion)).thenReturn(true);
        when(loginClient.obtenerSesion(idSesion)).thenReturn((Map) Map.of("idUsuario", 10L));

        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null);
        when(carritoRepository.findByIdUsuarioAndEstado(10L, EstadoCarrito.activo))
            .thenReturn(Optional.of(carrito));

        Carrito result = carritoService.revisarCarrito(idSesion);
        assertThat(result).isSameAs(carrito);
    }

    @Test
    void verCatalogo_sinBusqueda() {
        List<Map<String, Object>> productos = List.of(
            Map.of("id", 1L, "nombre", "Libro A"),
            Map.of("id", 2L, "nombre", "Libro B")
        );
        when(inventarioClient.listarProductos()).thenReturn(productos);

        List<Map<String, Object>> result = carritoService.verCatalogo(null, null, true);
        assertThat(result).hasSize(2);
    }

    @Test
    void verCatalogo_conBusqueda() {
        List<Map<String, Object>> productos = List.of(
            Map.of("id", 1L, "nombre", "Java Programming"),
            Map.of("id", 2L, "nombre", "Python Basics")
        );
        when(inventarioClient.listarProductos()).thenReturn(productos);

        List<Map<String, Object>> result = carritoService.verCatalogo("java", null, true);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("nombre")).isEqualTo("Java Programming");
    }

    @Test
    void verDetalleProducto() {
        when(inventarioClient.buscarProductoPorId(1L))
            .thenReturn(new HashMap<>(Map.of("id", 1L, "nombre", "Libro X")));
        when(inventarioClient.mostrarStockTotal(1L))
            .thenReturn(Map.of("stockTotal", 10));

        Map<String, Object> result = carritoService.verDetalleProducto(1L);
        assertThat(result.get("stockTotal")).isEqualTo(10);
    }

    @Test
    void verDetalleProducto_noEncontrado_retornaNull() {
        when(inventarioClient.buscarProductoPorId(1L)).thenReturn(null);
        assertThat(carritoService.verDetalleProducto(1L)).isNull();
    }

    @Test
    void guardar() {
        Carrito carrito = new Carrito();
        when(carritoRepository.save(carrito)).thenReturn(carrito);
        assertThat(carritoService.guardar(carrito)).isSameAs(carrito);
    }

    @Test
    void obtenerPorId() {
        Carrito carrito = new Carrito();
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        assertThat(carritoService.obtenerPorId(1L)).containsSame(carrito);
    }

    @Test
    void obtenerTodos() {
        when(carritoRepository.findAll()).thenReturn(List.of(new Carrito()));
        assertThat(carritoService.obtenerTodos()).hasSize(1);
    }

    @Test
    void actualizar() {
        Carrito existente = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null);
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(existente));

        Carrito cambios = new Carrito();
        cambios.setIdUsuario(20L);
        cambios.setEstado(EstadoCarrito.abandonado);

        Carrito merged = new Carrito(1L, 20L, existente.getFechaCreacion(), null, EstadoCarrito.abandonado, null);
        when(carritoRepository.save(any())).thenReturn(merged);

        Carrito result = carritoService.actualizar(1L, cambios);
        assertThat(result.getIdUsuario()).isEqualTo(20L);
        assertThat(result.getEstado()).isEqualTo(EstadoCarrito.abandonado);
    }

    @Test
    void eliminar() {
        carritoService.eliminar(1L);
        verify(carritoRepository).deleteById(1L);
    }
}
