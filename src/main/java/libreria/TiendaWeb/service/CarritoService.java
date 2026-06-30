package libreria.TiendaWeb.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import libreria.TiendaWeb.client.InventarioClient;
import libreria.TiendaWeb.client.LoginClient;
import libreria.TiendaWeb.model.Carrito;
import libreria.TiendaWeb.model.ItemCarrito;
import libreria.TiendaWeb.model.enums.EstadoCarrito;
import libreria.TiendaWeb.repository.CarritoRepository;
import libreria.TiendaWeb.repository.ItemCarritoRepository;

@Service
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ItemCarritoRepository itemCarritoRepository;

    @Autowired
    private LoginClient loginClient;

    @Autowired
    private InventarioClient inventarioClient;

    public List<Map<String, Object>> verCatalogo(String busqueda, String orden, boolean ascendente) {
        List<Map<String, Object>> productos = inventarioClient.listarProductos();
        if (busqueda != null && !busqueda.isBlank()) {
            String q = busqueda.toLowerCase();
            productos = productos.stream()
                .filter(p -> p.containsKey("nombre") && ((String) p.get("nombre")).toLowerCase().contains(q))
                .collect(Collectors.toList());
        }
        return productos;
    }

    public Map<String, Object> verDetalleProducto(Long idProducto) {
        Map<String, Object> producto = inventarioClient.buscarProductoPorId(idProducto);
        if (producto == null) return null;
        Map<String, Object> stock = inventarioClient.mostrarStockTotal(idProducto);
        producto.put("stockTotal", stock != null ? stock.get("stockTotal") : 0);
        return producto;
    }

    @Transactional
    public Carrito agregarAlCarrito(Long idSesion, Long idProducto, int cantidad) {
        if (!loginClient.validarSesion(idSesion)) {
            throw new RuntimeException("Sesion no valida");
        }
        Map<?, ?> sesion = loginClient.obtenerSesion(idSesion);
        Long idUsuario = Long.valueOf(sesion.get("idUsuario").toString());

        Carrito carrito = carritoRepository.findByIdUsuarioAndEstado(idUsuario, EstadoCarrito.activo)
            .orElseGet(() -> {
                Carrito nuevo = new Carrito();
                nuevo.setIdUsuario(idUsuario);
                nuevo.setFechaCreacion(LocalDateTime.now());
                nuevo.setEstado(EstadoCarrito.activo);
                return carritoRepository.save(nuevo);
            });

        if (!inventarioClient.verificarStockSuficiente(idProducto, cantidad)) {
            throw new RuntimeException("Stock insuficiente");
        }

        Optional<ItemCarrito> existente = itemCarritoRepository.findByCarritoIdAndIdProducto(carrito.getId(), idProducto);
        if (existente.isPresent()) {
            ItemCarrito item = existente.get();
            item.setCantidad(item.getCantidad() + cantidad);
            itemCarritoRepository.save(item);
        } else {
            Map<String, Object> detalle = inventarioClient.obtenerPrecioYDetalle(idProducto);
            ItemCarrito nuevo = new ItemCarrito();
            nuevo.setIdProducto(idProducto);
            nuevo.setNombreProducto(detalle != null ? (String) detalle.get("nombre") : null);
            nuevo.setCantidad(cantidad);
            nuevo.setPrecioUnitario(detalle != null ? Double.valueOf(detalle.get("precioVenta").toString()) : 0.0);
            nuevo.setCarrito(carrito);
            itemCarritoRepository.save(nuevo);
        }

        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito modificarItemCarrito(Long idSesion, Long idProducto, int cantidad) {
        if (!loginClient.validarSesion(idSesion)) {
            throw new RuntimeException("Sesion no valida");
        }
        if (cantidad <= 0) {
            throw new RuntimeException("Cantidad debe ser mayor a 0");
        }
        if (!inventarioClient.verificarStockSuficiente(idProducto, cantidad)) {
            throw new RuntimeException("Stock insuficiente");
        }

        Map<?, ?> sesion = loginClient.obtenerSesion(idSesion);
        Long idUsuario = Long.valueOf(sesion.get("idUsuario").toString());
        Carrito carrito = carritoRepository.findByIdUsuarioAndEstado(idUsuario, EstadoCarrito.activo)
            .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        ItemCarrito item = itemCarritoRepository.findByCarritoIdAndIdProducto(carrito.getId(), idProducto)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado en el carrito"));
        item.setCantidad(cantidad);
        itemCarritoRepository.save(item);

        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    @Transactional
    public void eliminarItemCarrito(Long idSesion, Long idItemCarrito) {
        if (!loginClient.validarSesion(idSesion)) {
            throw new RuntimeException("Sesion no valida");
        }
        ItemCarrito item = itemCarritoRepository.findById(idItemCarrito)
            .orElseThrow(() -> new RuntimeException("Item no encontrado"));
        itemCarritoRepository.delete(item);
    }

    public Carrito revisarCarrito(Long idSesion) {
        if (!loginClient.validarSesion(idSesion)) {
            throw new RuntimeException("Sesion no valida");
        }
        Map<?, ?> sesion = loginClient.obtenerSesion(idSesion);
        Long idUsuario = Long.valueOf(sesion.get("idUsuario").toString());
        return carritoRepository.findByIdUsuarioAndEstado(idUsuario, EstadoCarrito.activo)
            .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));
    }

    public Carrito guardar(Carrito carrito) {
        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito actualizar(Long id, Carrito carrito) {
        Carrito existente = carritoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));
        existente.setIdUsuario(carrito.getIdUsuario());
        existente.setEstado(carrito.getEstado());
        existente.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(existente);
    }

    public Optional<Carrito> obtenerPorId(Long id) {
        return carritoRepository.findById(id);
    }

    public List<Carrito> obtenerTodos() {
        return carritoRepository.findAll();
    }

    @Transactional
    public void eliminar(Long id) {
        carritoRepository.deleteById(id);
    }
}
