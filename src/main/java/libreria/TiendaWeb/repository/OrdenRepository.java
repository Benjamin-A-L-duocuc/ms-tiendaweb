package libreria.TiendaWeb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import libreria.TiendaWeb.model.Orden;
import libreria.TiendaWeb.model.enums.EstadoOrden;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByIdUsuario(Long idUsuario);
    List<Orden> findByIdUsuarioAndEstado(Long idUsuario, EstadoOrden estado);
}
