package libreria.TiendaWeb.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import libreria.TiendaWeb.model.Carrito;
import libreria.TiendaWeb.model.enums.EstadoCarrito;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    Optional<Carrito> findByIdUsuarioAndEstado(Long idUsuario, EstadoCarrito estado);
    Optional<Carrito> findByIdUsuario(Long idUsuario);
}
