package libreria.TiendaWeb.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import libreria.TiendaWeb.model.ItemCarrito;
import libreria.TiendaWeb.service.ItemCarritoService;

@RestController
@RequestMapping("/api/v1/items-carrito")
public class ItemCarritoController {

    @Autowired
    private ItemCarritoService itemCarritoService;

    @PostMapping
    public ResponseEntity<ItemCarrito> crear(@RequestBody ItemCarrito itemCarrito) {
        ItemCarrito guardado = itemCarritoService.guardar(itemCarrito);
        return ResponseEntity.ok(guardado);
    }

    @GetMapping
    public ResponseEntity<List<ItemCarrito>> obtenerTodos() {
        return ResponseEntity.ok(itemCarritoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemCarrito> obtenerPorId(@PathVariable Long id) {
        return itemCarritoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemCarrito> actualizar(@PathVariable Long id, @RequestBody ItemCarrito itemCarrito) {
        try {
            ItemCarrito actualizado = itemCarritoService.actualizar(id, itemCarrito);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        itemCarritoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
