package libreria.TiendaWeb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import libreria.TiendaWeb.model.Carrito;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
}
