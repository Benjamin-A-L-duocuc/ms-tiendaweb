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

import libreria.TiendaWeb.model.Orden;
import libreria.TiendaWeb.model.enums.MetodoContacto;
import libreria.TiendaWeb.service.OrdenService;

@RestController
@RequestMapping("/api/v1/tienda")
public class OrdenController {

    @Autowired
    private OrdenService ordenService;

    @PostMapping("/ordenes")
    public ResponseEntity<?> crearOrden(@RequestBody Orden orden, @RequestParam Long idSesion) {
        try {
            return ResponseEntity.ok(ordenService.crearOrden(orden, idSesion));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/ordenes/{id}/venta")
    public ResponseEntity<?> hacerVenta(
            @PathVariable Long id,
            @RequestParam Long idSesion,
            @RequestParam(required = false) Long idDescuento) {
        try {
            return ResponseEntity.ok(ordenService.hacerVenta(id, idSesion, idDescuento));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pedidos")
    public ResponseEntity<?> verPedidos(@RequestParam Long idSesion) {
        try {
            List<Orden> pedidos = ordenService.verPedidos(idSesion);
            return ResponseEntity.ok(pedidos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> verPerfil(@RequestParam Long idSesion) {
        try {
            return ResponseEntity.ok(ordenService.verPerfil(idSesion));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/soporte")
    public ResponseEntity<?> contactarSoporte(@RequestParam MetodoContacto metodo) {
        return ResponseEntity.ok(ordenService.contactarSoporte(metodo));
    }

    @GetMapping("/ordenes/{id}/envio")
    public ResponseEntity<?> verEnvioPedido(
            @PathVariable Long id,
            @RequestParam Long idSesion) {
        try {
            return ResponseEntity.ok(ordenService.verEnvioPedido(idSesion, id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/ordenes")
    public ResponseEntity<List<Orden>> obtenerTodas() {
        return ResponseEntity.ok(ordenService.obtenerTodas());
    }

    @GetMapping("/ordenes/{id}")
    public ResponseEntity<Orden> obtenerPorId(@PathVariable Long id) {
        return ordenService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/ordenes/{id}")
    public ResponseEntity<Orden> actualizar(@PathVariable Long id, @RequestBody Orden orden) {
        try {
            return ResponseEntity.ok(ordenService.actualizar(id, orden));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/ordenes/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ordenService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
