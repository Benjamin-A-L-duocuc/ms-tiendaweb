package libreria.TiendaWeb.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import libreria.TiendaWeb.model.enums.MetodoContacto;
import libreria.TiendaWeb.repository.CarritoRepository;
import libreria.TiendaWeb.repository.ItemCarritoRepository;
import libreria.TiendaWeb.repository.OrdenRepository;

@Service
public class OrdenService {

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ItemCarritoRepository itemCarritoRepository;

    @Autowired
    private LoginClient loginClient;

    @Autowired
    private InventarioClient inventarioClient;

    @Autowired
    private VentasClient ventasClient;

    @Autowired
    private EnvioClient envioClient;

    @Autowired
    private RegistroUsuarioClient registroUsuarioClient;

    @Transactional
    public Orden crearOrden(Orden orden, Long idSesion) {
        if (!loginClient.validarSesion(idSesion)) {
            throw new RuntimeException("Sesion no valida");
        }
        Map<?, ?> sesion = loginClient.obtenerSesion(idSesion);
        Long idUsuario = Long.valueOf(sesion.get("idUsuario").toString());

        Carrito carrito = carritoRepository.findByIdUsuarioAndEstado(idUsuario, EstadoCarrito.activo)
            .orElseThrow(() -> new RuntimeException("Carrito vacio o no encontrado"));

        List<ItemCarrito> items = itemCarritoRepository.findByCarritoId(carrito.getId());
        if (items.isEmpty()) {
            throw new RuntimeException("Carrito vacio");
        }

        orden.setIdUsuario(idUsuario);
        orden.setFechaOrden(LocalDateTime.now());

        double subtotal = items.stream()
            .mapToDouble(i -> i.getPrecioUnitario() * i.getCantidad())
            .sum();
        double impuestos = subtotal * 0.19;
        double total = subtotal + impuestos;

        orden.setSubtotal((float) subtotal);
        orden.setImpuestos((float) impuestos);
        orden.setTotal((float) total);
        orden.setEstado(EstadoOrden.PENDIENTE_PAGO);

        Orden saved = ordenRepository.save(orden);

        carrito.setEstado(EstadoCarrito.convertido);
        carrito.setFechaActualizacion(LocalDateTime.now());
        carritoRepository.save(carrito);

        return saved;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Map<String, Object> hacerVenta(Long idOrden, Long idSesion, Long idDescuento) {
        if (!loginClient.validarSesion(idSesion)) {
            throw new RuntimeException("Sesion no valida");
        }
        Map<?, ?> sesion = loginClient.obtenerSesion(idSesion);
        Long idUsuario = Long.valueOf(sesion.get("idUsuario").toString());

        Orden orden = ordenRepository.findById(idOrden)
            .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        if (!orden.getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("La orden no pertenece al usuario");
        }

        if (idDescuento != null) {
            Map<String, Object> descuento = ventasClient.buscarDescuentoPorId(idDescuento);
            if (descuento == null || !Boolean.TRUE.equals(descuento.get("activo"))) {
                throw new RuntimeException("Descuento no valido o inactivo");
            }
        }

        List<ItemCarrito> items = itemCarritoRepository.findByCarritoId(
            carritoRepository.findByIdUsuarioAndEstado(idUsuario, EstadoCarrito.convertido)
                .map(Carrito::getId).orElse(null));

        if (items == null || items.isEmpty()) {
            throw new RuntimeException("No hay items en la orden");
        }

        for (ItemCarrito item : items) {
            if (!inventarioClient.verificarStockSuficiente(item.getIdProducto(), item.getCantidad())) {
                throw new RuntimeException("Stock insuficiente para producto " + item.getIdProducto());
            }
        }

        for (ItemCarrito item : items) {
            inventarioClient.reservarStock(item.getIdProducto(), item.getCantidad(), idOrden);
        }

        Map<String, Object> ventaRequest = new HashMap<>();
        ventaRequest.put("idOrden", idOrden);
        ventaRequest.put("idDescuento", idDescuento);
        ventaRequest.put("idUsuario", idUsuario);
        Map<String, Object> ventaResponse = ventasClient.crearVentaOnline(ventaRequest);

        orden.setEstado(EstadoOrden.PAGADA);
        ordenRepository.save(orden);

        Map<String, Object> result = new HashMap<>();
        result.put("orden", orden);
        result.put("venta", ventaResponse);
        return result;
    }

    public List<Orden> verPedidos(Long idSesion) {
        if (!loginClient.validarSesion(idSesion)) {
            throw new RuntimeException("Sesion no valida");
        }
        Map<?, ?> sesion = loginClient.obtenerSesion(idSesion);
        Long idUsuario = Long.valueOf(sesion.get("idUsuario").toString());
        return ordenRepository.findByIdUsuario(idUsuario);
    }

    public Map<String, Object> verPerfil(Long idSesion) {
        if (!loginClient.validarSesion(idSesion)) {
            throw new RuntimeException("Sesion no valida");
        }
        Map<?, ?> sesion = loginClient.obtenerSesion(idSesion);
        Long idUsuario = Long.valueOf(sesion.get("idUsuario").toString());
        Map<String, Object> usuario = registroUsuarioClient.consultarUsuario(idUsuario);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }
        return usuario;
    }

    public Map<String, String> contactarSoporte(MetodoContacto metodo) {
        Map<String, String> response = new HashMap<>();
        if (metodo == MetodoContacto.Telefono) {
            response.put("medio", "Telefono");
            response.put("contacto", "+56 2 1234 5678");
        } else {
            response.put("medio", "Correo");
            response.put("contacto", "soporte@libreria.cl");
        }
        return response;
    }

    public Map<String, Object> verEnvioPedido(Long idSesion, Long idOrden) {
        if (!loginClient.validarSesion(idSesion)) {
            throw new RuntimeException("Sesion no valida");
        }
        Map<?, ?> sesion = loginClient.obtenerSesion(idSesion);
        Long idUsuario = Long.valueOf(sesion.get("idUsuario").toString());

        Orden orden = ordenRepository.findById(idOrden)
            .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        if (!orden.getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("La orden no pertenece al usuario");
        }
        if (orden.getEstado() != EstadoOrden.ENVIADA && orden.getEstado() != EstadoOrden.ENTREGADA) {
            throw new RuntimeException("La orden no ha sido enviada");
        }

        Map<String, Object> envio = envioClient.buscarEnvioPorOrden(idOrden);
        if (envio == null) {
            throw new RuntimeException("Envio no encontrado");
        }
        return envio;
    }

    public Orden guardar(Orden orden) {
        return ordenRepository.save(orden);
    }

    public Optional<Orden> obtenerPorId(Long id) {
        return ordenRepository.findById(id);
    }

    public List<Orden> obtenerTodas() {
        return ordenRepository.findAll();
    }

    @Transactional
    public Orden actualizar(Long id, Orden orden) {
        Orden existente = ordenRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
        existente.setSubtotal(orden.getSubtotal());
        existente.setImpuestos(orden.getImpuestos());
        existente.setTotal(orden.getTotal());
        existente.setMedioPago(orden.getMedioPago());
        existente.setEstado(orden.getEstado());
        existente.setDireccionEnvio(orden.getDireccionEnvio());
        existente.setDireccionFacturacion(orden.getDireccionFacturacion());
        return ordenRepository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        ordenRepository.deleteById(id);
    }
}
