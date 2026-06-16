package libreria.TiendaWeb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import libreria.TiendaWeb.model.Resena;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {
}
