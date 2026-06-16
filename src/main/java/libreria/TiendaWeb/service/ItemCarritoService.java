package libreria.TiendaWeb.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import libreria.TiendaWeb.model.ItemCarrito;
import libreria.TiendaWeb.repository.ItemCarritoRepository;

@Service
public class ItemCarritoService {

    @Autowired
    private ItemCarritoRepository itemCarritoRepository;

    @Transactional
    public ItemCarrito guardar(ItemCarrito itemCarrito) {
        return itemCarritoRepository.save(itemCarrito);
    }

    public Optional<ItemCarrito> obtenerPorId(Long id) {
        return itemCarritoRepository.findById(id);
    }

    public List<ItemCarrito> obtenerTodos() {
        return itemCarritoRepository.findAll();
    }

    @Transactional
    public ItemCarrito actualizar(Long id, ItemCarrito itemCarrito) {
        ItemCarrito existente = itemCarritoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ItemCarrito no encontrado"));

        existente.setCantidad(itemCarrito.getCantidad());
        existente.setCarritoId(itemCarrito.getCarritoId());

        return itemCarritoRepository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        itemCarritoRepository.deleteById(id);
    }
}
