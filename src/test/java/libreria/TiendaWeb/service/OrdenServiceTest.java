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

import libreria.TiendaWeb.client.EnvioClient;
import libreria.TiendaWeb.client.InventarioClient;
import libreria.TiendaWeb.client.LoginClient;
import libreria.TiendaWeb.client.RegistroUsuarioClient;
import libreria.TiendaWeb.client.VentasClient;
import libreria.TiendaWeb.model.Carrito;
import libreria.TiendaWeb.model.ItemCarrito;
import libreria.TiendaWeb.model.Orden;
import libreria.TiendaWeb.model.enums.EstadoCarrito;
import libreria.TiendaWeb.model.enums.EstadoOrden;
import libreria.TiendaWeb.model.enums.MedioPago;
import libreria.TiendaWeb.model.enums.MetodoContacto;
import libreria.TiendaWeb.repository.CarritoRepository;
import libreria.TiendaWeb.repository.ItemCarritoRepository;
import libreria.TiendaWeb.repository.OrdenRepository;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    @Mock
    private OrdenRepository ordenRepository;

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ItemCarritoRepository itemCarritoRepository;

    @Mock
    private LoginClient loginClient;

    @Mock
    private InventarioClient inventarioClient;

    @Mock
    private VentasClient ventasClient;

    @Mock
    private EnvioClient envioClient;

    @Mock
    private RegistroUsuarioClient registroUsuarioClient;

    @InjectMocks
    private OrdenService ordenService;

    @SuppressWarnings("unchecked")
    private void setupSession(Long idSesion, Long idUsuario) {
        when(loginClient.validarSesion(idSesion)).thenReturn(true);
        Map<String, Object> sesion = new HashMap<>();
        sesion.put("idUsuario", idUsuario);
        when(loginClient.obtenerSesion(idSesion)).thenReturn((Map) sesion);
    }

    @Test
    void crearOrden_sesionInvalida_deberiaLanzarExcepcion() {
        when(loginClient.validarSesion(1L)).thenReturn(false);

        assertThatThrownBy(() -> ordenService.crearOrden(new Orden(), 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sesion no valida");
    }

    @Test
    void crearOrden_carritoVacio_deberiaLanzarExcepcion() {
        setupSession(1L, 10L);
        when(carritoRepository.findByIdUsuarioAndEstado(10L, EstadoCarrito.activo))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenService.crearOrden(new Orden(), 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Carrito vacio");
    }

    @Test
    void crearOrden_sinItems_deberiaLanzarExcepcion() {
        setupSession(1L, 10L);
        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null);
        when(carritoRepository.findByIdUsuarioAndEstado(10L, EstadoCarrito.activo))
                .thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> ordenService.crearOrden(new Orden(), 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Carrito vacio");
    }

    @Test
    void crearOrden_conItems_deberiaCrearOrden() {
        setupSession(1L, 10L);
        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.activo, null);
        when(carritoRepository.findByIdUsuarioAndEstado(10L, EstadoCarrito.activo))
                .thenReturn(Optional.of(carrito));

        ItemCarrito item = new ItemCarrito(1L, 100L, "Libro A", 2, 5000.0, carrito);
        when(itemCarritoRepository.findByCarritoId(1L)).thenReturn(List.of(item));

        Orden saved = new Orden();
        saved.setId(1L);
        saved.setIdUsuario(10L);
        saved.setEstado(EstadoOrden.PENDIENTE_PAGO);
        when(ordenRepository.save(any(Orden.class))).thenReturn(saved);
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        Orden result = ordenService.crearOrden(new Orden(), 1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo(EstadoOrden.PENDIENTE_PAGO);
        verify(ordenRepository).save(any(Orden.class));
        verify(carritoRepository).save(carrito);
    }

    @Test
    void verPedidos_sesionInvalida_deberiaLanzarExcepcion() {
        when(loginClient.validarSesion(1L)).thenReturn(false);

        assertThatThrownBy(() -> ordenService.verPedidos(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sesion no valida");
    }

    @Test
    void verPedidos_deberiaRetornarOrdenes() {
        setupSession(1L, 10L);
        when(ordenRepository.findByIdUsuario(10L)).thenReturn(List.of(new Orden(), new Orden()));

        List<Orden> result = ordenService.verPedidos(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    void verPerfil_sesionInvalida_deberiaLanzarExcepcion() {
        when(loginClient.validarSesion(1L)).thenReturn(false);

        assertThatThrownBy(() -> ordenService.verPerfil(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sesion no valida");
    }

    @Test
    void verPerfil_usuarioNoEncontrado_deberiaLanzarExcepcion() {
        setupSession(1L, 10L);
        when(registroUsuarioClient.consultarUsuario(10L)).thenReturn(null);

        assertThatThrownBy(() -> ordenService.verPerfil(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado");
    }

    @Test
    void verPerfil_deberiaRetornarUsuario() {
        setupSession(1L, 10L);
        Map<String, Object> usuario = Map.of("id", 10L, "nombre", "Juan");
        when(registroUsuarioClient.consultarUsuario(10L)).thenReturn(usuario);

        Map<String, Object> result = ordenService.verPerfil(1L);

        assertThat(result.get("nombre")).isEqualTo("Juan");
    }

    @Test
    void contactarSoporte_telefono() {
        Map<String, String> result = ordenService.contactarSoporte(MetodoContacto.Telefono);

        assertThat(result.get("medio")).isEqualTo("Telefono");
        assertThat(result.get("contacto")).isEqualTo("+56 2 1234 5678");
    }

    @Test
    void contactarSoporte_correo() {
        Map<String, String> result = ordenService.contactarSoporte(MetodoContacto.Correo);

        assertThat(result.get("medio")).isEqualTo("Correo");
        assertThat(result.get("contacto")).isEqualTo("soporte@libreria.cl");
    }

    @Test
    void guardar_deberiaRetornarOrdenGuardada() {
        Orden orden = new Orden();
        when(ordenRepository.save(orden)).thenReturn(orden);

        Orden result = ordenService.guardar(orden);

        assertThat(result).isSameAs(orden);
        verify(ordenRepository).save(orden);
    }

    @Test
    void obtenerPorId_deberiaRetornarOptional() {
        Orden orden = new Orden();
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        Optional<Orden> result = ordenService.obtenerPorId(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(orden);
    }

    @Test
    void obtenerTodas_deberiaRetornarLista() {
        when(ordenRepository.findAll()).thenReturn(List.of(new Orden(), new Orden()));

        List<Orden> result = ordenService.obtenerTodas();

        assertThat(result).hasSize(2);
    }

    @Test
    void actualizar_noEncontrada_deberiaLanzarExcepcion() {
        when(ordenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenService.actualizar(99L, new Orden()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Orden no encontrada");
    }

    @Test
    void eliminar_deberiaLlamarDeleteById() {
        ordenService.eliminar(1L);
        verify(ordenRepository).deleteById(1L);
    }

    @Test
    void hacerVenta_sesionInvalida_deberiaLanzarExcepcion() {
        when(loginClient.validarSesion(1L)).thenReturn(false);

        assertThatThrownBy(() -> ordenService.hacerVenta(1L, 1L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sesion no valida");
    }

    @Test
    void hacerVenta_ordenNoEncontrada_deberiaLanzarExcepcion() {
        setupSession(1L, 10L);
        when(ordenRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenService.hacerVenta(1L, 1L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Orden no encontrada");
    }

    @Test
    void hacerVenta_ordenNoPerteneceAlUsuario_deberiaLanzarExcepcion() {
        setupSession(1L, 10L);
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setIdUsuario(20L);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        assertThatThrownBy(() -> ordenService.hacerVenta(1L, 1L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La orden no pertenece al usuario");
    }

    @Test
    void hacerVenta_descuentoInactivo_deberiaLanzarExcepcion() {
        setupSession(1L, 10L);
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setIdUsuario(10L);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ventasClient.buscarDescuentoPorId(5L)).thenReturn(Map.of("activo", false));

        assertThatThrownBy(() -> ordenService.hacerVenta(1L, 1L, 5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Descuento no valido o inactivo");
    }

    @Test
    void hacerVenta_descuentoNulo_deberiaLanzarExcepcion() {
        setupSession(1L, 10L);
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setIdUsuario(10L);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ventasClient.buscarDescuentoPorId(5L)).thenReturn(null);

        assertThatThrownBy(() -> ordenService.hacerVenta(1L, 1L, 5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Descuento no valido o inactivo");
    }

    @Test
    void hacerVenta_sinItems_deberiaLanzarExcepcion() {
        setupSession(1L, 10L);
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setIdUsuario(10L);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.convertido, null);
        when(carritoRepository.findByIdUsuarioAndEstado(10L, EstadoCarrito.convertido))
                .thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> ordenService.hacerVenta(1L, 1L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No hay items en la orden");
    }

    @Test
    void hacerVenta_itemsNulos_deberiaLanzarExcepcion() {
        setupSession(1L, 10L);
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setIdUsuario(10L);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.convertido, null);
        when(carritoRepository.findByIdUsuarioAndEstado(10L, EstadoCarrito.convertido))
                .thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoId(1L)).thenReturn(null);

        assertThatThrownBy(() -> ordenService.hacerVenta(1L, 1L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No hay items en la orden");
    }

    @Test
    void hacerVenta_stockInsuficiente_deberiaLanzarExcepcion() {
        setupSession(1L, 10L);
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setIdUsuario(10L);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.convertido, null);
        when(carritoRepository.findByIdUsuarioAndEstado(10L, EstadoCarrito.convertido))
                .thenReturn(Optional.of(carrito));

        ItemCarrito item = new ItemCarrito(1L, 100L, "Libro A", 5, 5000.0, carrito);
        when(itemCarritoRepository.findByCarritoId(1L)).thenReturn(List.of(item));
        when(inventarioClient.verificarStockSuficiente(100L, 5)).thenReturn(false);

        assertThatThrownBy(() -> ordenService.hacerVenta(1L, 1L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void hacerVenta_conExito_deberiaReservarStockYCrearVenta() {
        setupSession(1L, 10L);
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setIdUsuario(10L);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.convertido, null);
        when(carritoRepository.findByIdUsuarioAndEstado(10L, EstadoCarrito.convertido))
                .thenReturn(Optional.of(carrito));

        ItemCarrito item = new ItemCarrito(1L, 100L, "Libro A", 2, 5000.0, carrito);
        when(itemCarritoRepository.findByCarritoId(1L)).thenReturn(List.of(item));
        when(inventarioClient.verificarStockSuficiente(100L, 2)).thenReturn(true);
        when(ventasClient.crearVentaOnline(any())).thenReturn(Map.of("idVenta", 1L));

        Map<String, Object> result = ordenService.hacerVenta(1L, 1L, null);

        assertThat(result.get("orden")).isNotNull();
        assertThat(result.get("venta")).isNotNull();
        verify(inventarioClient).reservarStock(100L, 2, 1L);
        verify(ordenRepository).save(any(Orden.class));
    }

    @Test
    void hacerVenta_conDescuentoActivo_deberiaPermitirVenta() {
        setupSession(1L, 10L);
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setIdUsuario(10L);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ventasClient.buscarDescuentoPorId(5L)).thenReturn(Map.of("activo", true));

        Carrito carrito = new Carrito(1L, 10L, LocalDateTime.now(), null, EstadoCarrito.convertido, null);
        when(carritoRepository.findByIdUsuarioAndEstado(10L, EstadoCarrito.convertido))
                .thenReturn(Optional.of(carrito));

        ItemCarrito item = new ItemCarrito(1L, 100L, "Libro A", 1, 5000.0, carrito);
        when(itemCarritoRepository.findByCarritoId(1L)).thenReturn(List.of(item));
        when(inventarioClient.verificarStockSuficiente(100L, 1)).thenReturn(true);
        when(ventasClient.crearVentaOnline(any())).thenReturn(Map.of("idVenta", 2L));

        Map<String, Object> result = ordenService.hacerVenta(1L, 1L, 5L);

        assertThat(result.get("orden")).isNotNull();
        verify(ventasClient).buscarDescuentoPorId(5L);
    }

    @Test
    void verEnvioPedido_sesionInvalida_deberiaLanzarExcepcion() {
        when(loginClient.validarSesion(1L)).thenReturn(false);

        assertThatThrownBy(() -> ordenService.verEnvioPedido(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sesion no valida");
    }

    @Test
    void verEnvioPedido_ordenNoEncontrada_deberiaLanzarExcepcion() {
        setupSession(1L, 10L);
        when(ordenRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenService.verEnvioPedido(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Orden no encontrada");
    }

    @Test
    void verEnvioPedido_noPerteneceAlUsuario_deberiaLanzarExcepcion() {
        setupSession(1L, 10L);
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setIdUsuario(20L);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        assertThatThrownBy(() -> ordenService.verEnvioPedido(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La orden no pertenece al usuario");
    }

    @Test
    void verEnvioPedido_noEnviada_deberiaLanzarExcepcion() {
        setupSession(1L, 10L);
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setIdUsuario(10L);
        orden.setEstado(EstadoOrden.PENDIENTE_PAGO);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        assertThatThrownBy(() -> ordenService.verEnvioPedido(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La orden no ha sido enviada");
    }

    @Test
    void verEnvioPedido_envioNoEncontrado_deberiaLanzarExcepcion() {
        setupSession(1L, 10L);
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setIdUsuario(10L);
        orden.setEstado(EstadoOrden.ENVIADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(envioClient.buscarEnvioPorOrden(1L)).thenReturn(null);

        assertThatThrownBy(() -> ordenService.verEnvioPedido(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Envio no encontrado");
    }

    @Test
    void verEnvioPedido_entregada_deberiaRetornarEnvio() {
        setupSession(1L, 10L);
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setIdUsuario(10L);
        orden.setEstado(EstadoOrden.ENTREGADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(envioClient.buscarEnvioPorOrden(1L)).thenReturn(Map.of("idEnvio", 1L, "estado", "entregado"));

        Map<String, Object> result = ordenService.verEnvioPedido(1L, 1L);

        assertThat(result.get("idEnvio")).isEqualTo(1L);
    }

    @Test
    void verEnvioPedido_enviada_deberiaRetornarEnvio() {
        setupSession(1L, 10L);
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setIdUsuario(10L);
        orden.setEstado(EstadoOrden.ENVIADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(envioClient.buscarEnvioPorOrden(1L)).thenReturn(Map.of("idEnvio", 2L, "estado", "en_transito"));

        Map<String, Object> result = ordenService.verEnvioPedido(1L, 1L);

        assertThat(result.get("idEnvio")).isEqualTo(2L);
    }
}
