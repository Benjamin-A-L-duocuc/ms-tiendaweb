package libreria.TiendaWeb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import libreria.TiendaWeb.model.Orden;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {
}
