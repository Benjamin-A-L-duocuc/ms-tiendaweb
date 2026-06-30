package libreria.TiendaWeb.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import libreria.TiendaWeb.model.Carrito;
import libreria.TiendaWeb.service.CarritoService;

@RestController
@RequestMapping("/api/v1/tienda")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @GetMapping("/catalogo")
    public ResponseEntity<List<Map<String, Object>>> verCatalogo(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String orden,
            @RequestParam(defaultValue = "true") boolean ascendente) {
        return ResponseEntity.ok(carritoService.verCatalogo(busqueda, orden, ascendente));
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<Map<String, Object>> verDetalleProducto(@PathVariable Long id) {
        Map<String, Object> producto = carritoService.verDetalleProducto(id);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(producto);
    }

    @PostMapping("/carrito/agregar")
    public ResponseEntity<?> agregarAlCarrito(
            @RequestParam Long idSesion,
            @RequestParam Long idProducto,
            @RequestParam int cantidad) {
        try {
            Carrito carrito = carritoService.agregarAlCarrito(idSesion, idProducto, cantidad);
            return ResponseEntity.ok(carrito);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/carrito/modificar")
    public ResponseEntity<?> modificarItemCarrito(
            @RequestParam Long idSesion,
            @RequestParam Long idProducto,
            @RequestParam int cantidad) {
        try {
            Carrito carrito = carritoService.modificarItemCarrito(idSesion, idProducto, cantidad);
            return ResponseEntity.ok(carrito);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/carrito/items/{idItem}")
    public ResponseEntity<?> eliminarItemCarrito(
            @RequestParam Long idSesion,
            @PathVariable Long idItem) {
        try {
            carritoService.eliminarItemCarrito(idSesion, idItem);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/carrito")
    public ResponseEntity<?> revisarCarrito(@RequestParam Long idSesion) {
        try {
            Carrito carrito = carritoService.revisarCarrito(idSesion);
            return ResponseEntity.ok(carrito);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/carrito")
    public ResponseEntity<Carrito> crear(@RequestBody Carrito carrito) {
        return ResponseEntity.ok(carritoService.guardar(carrito));
    }

    @GetMapping("/carritos")
    public ResponseEntity<List<Carrito>> obtenerTodos() {
        return ResponseEntity.ok(carritoService.obtenerTodos());
    }

    @GetMapping("/carritos/{id}")
    public ResponseEntity<Carrito> obtenerPorId(@PathVariable Long id) {
        return carritoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/carritos/{id}")
    public ResponseEntity<Carrito> actualizar(@PathVariable Long id, @RequestBody Carrito carrito) {
        try {
            return ResponseEntity.ok(carritoService.actualizar(id, carrito));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/carritos/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        carritoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
