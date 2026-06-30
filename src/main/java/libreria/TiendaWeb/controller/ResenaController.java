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

import libreria.TiendaWeb.model.Resena;
import libreria.TiendaWeb.service.ResenaService;

@RestController
@RequestMapping("/api/v1/tienda")
public class ResenaController {

    @Autowired
    private ResenaService resenaService;

    @PostMapping("/resenas")
    public ResponseEntity<?> reseñarProducto(@RequestBody Resena resena, @RequestParam Long idSesion) {
        try {
            return ResponseEntity.ok(resenaService.reseñarProducto(resena, idSesion));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/resenas")
    public ResponseEntity<?> cambiarResena(@RequestBody Resena resena, @RequestParam Long idSesion) {
        try {
            return ResponseEntity.ok(resenaService.cambiarResena(resena, idSesion));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/productos/{idProducto}/resenas")
    public ResponseEntity<List<Resena>> obtenerResenasPorProducto(@PathVariable Long idProducto) {
        return ResponseEntity.ok(resenaService.obtenerResenasPorProducto(idProducto));
    }

    @GetMapping("/resenas")
    public ResponseEntity<List<Resena>> obtenerTodas() {
        return ResponseEntity.ok(resenaService.obtenerTodas());
    }

    @GetMapping("/resenas/{id}")
    public ResponseEntity<Resena> obtenerPorId(@PathVariable Long id) {
        return resenaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/resenas/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        resenaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
