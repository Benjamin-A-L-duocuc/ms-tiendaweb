package libreria.TiendaWeb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import libreria.TiendaWeb.model.ItemCarrito;

@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
    List<ItemCarrito> findByCarritoId(Long carritoId);
    Optional<ItemCarrito> findByCarritoIdAndIdProducto(Long carritoId, Long idProducto);
    void deleteByCarritoId(Long carritoId);
}
