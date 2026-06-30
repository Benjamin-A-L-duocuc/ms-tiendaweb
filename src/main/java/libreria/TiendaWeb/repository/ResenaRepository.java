package libreria.TiendaWeb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import libreria.TiendaWeb.model.Resena;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByIdProducto(Long idProducto);
    Optional<Resena> findByIdUsuarioAndIdProducto(Long idUsuario, Long idProducto);
}
