package libreria.TiendaWeb.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import libreria.TiendaWeb.model.Carrito;
import libreria.TiendaWeb.repository.CarritoRepository;

@Service
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Transactional
    public Carrito guardar(Carrito carrito) {
        return carritoRepository.save(carrito);
    }

    public Optional<Carrito> obtenerPorId(Long id) {
        return carritoRepository.findById(id);
    }

    public List<Carrito> obtenerTodos() {
        return carritoRepository.findAll();
    }

    @Transactional
    public Carrito actualizar(Long id, Carrito carrito) {
        Carrito existente = carritoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        existente.setIdUsuario(carrito.getIdUsuario());
        existente.setFechaCreacion(carrito.getFechaCreacion());
        existente.setFechaActualizacion(carrito.getFechaActualizacion());
        existente.setEstado(carrito.getEstado());

        return carritoRepository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        carritoRepository.deleteById(id);
    }
}
