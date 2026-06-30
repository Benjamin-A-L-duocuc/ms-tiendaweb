package libreria.TiendaWeb.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import libreria.TiendaWeb.client.LoginClient;
import libreria.TiendaWeb.model.Resena;
import libreria.TiendaWeb.model.enums.EstadoOrden;
import libreria.TiendaWeb.repository.OrdenRepository;
import libreria.TiendaWeb.repository.ResenaRepository;

@Service
public class ResenaService {

    private static final Set<String> PALABRAS_PROHIBIDAS = Set.of(
        "spam", "publicidad", "trampa", "estafa", "fraude");

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private LoginClient loginClient;

    @Transactional
    public Resena reseñarProducto(Resena resena, Long idSesion) {
        if (!loginClient.validarSesion(idSesion)) {
            throw new RuntimeException("Sesion no valida");
        }
        Map<?, ?> sesion = loginClient.obtenerSesion(idSesion);
        Long idUsuario = Long.valueOf(sesion.get("idUsuario").toString());

        boolean haComprado = ordenRepository.findByIdUsuario(idUsuario).stream()
            .anyMatch(o -> o.getEstado() == EstadoOrden.ENTREGADA);
        if (!haComprado) {
            throw new RuntimeException("Debe haber comprado el producto para reseñarlo");
        }

        if (resenaRepository.findByIdUsuarioAndIdProducto(idUsuario, resena.getIdProducto()).isPresent()) {
            throw new RuntimeException("Ya ha reseñado este producto");
        }

        if (resena.getComentario() != null) {
            String comentario = resena.getComentario().trim();
            if (comentario.length() < 20 || comentario.length() > 1000) {
                throw new RuntimeException("El comentario debe tener entre 20 y 1000 caracteres");
            }
            for (String palabra : PALABRAS_PROHIBIDAS) {
                if (comentario.toLowerCase().contains(palabra)) {
                    throw new RuntimeException("El comentario contiene palabras prohibidas");
                }
            }
        }

        resena.setIdUsuario(idUsuario);
        resena.setFecha(LocalDateTime.now());
        return resenaRepository.save(resena);
    }

    @Transactional
    public Resena cambiarResena(Resena resena, Long idSesion) {
        if (!loginClient.validarSesion(idSesion)) {
            throw new RuntimeException("Sesion no valida");
        }
        Map<?, ?> sesion = loginClient.obtenerSesion(idSesion);
        Long idUsuario = Long.valueOf(sesion.get("idUsuario").toString());

        Resena existente = resenaRepository.findById(resena.getId())
            .orElseThrow(() -> new RuntimeException("Resena no encontrada"));

        if (!existente.getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("No puede editar una resena que no le pertenece");
        }

        if (resena.getComentario() != null) {
            String comentario = resena.getComentario().trim();
            if (comentario.length() < 20 || comentario.length() > 1000) {
                throw new RuntimeException("El comentario debe tener entre 20 y 1000 caracteres");
            }
            for (String palabra : PALABRAS_PROHIBIDAS) {
                if (comentario.toLowerCase().contains(palabra)) {
                    throw new RuntimeException("El comentario contiene palabras prohibidas");
                }
            }
            existente.setComentario(comentario);
        }

        return resenaRepository.save(existente);
    }

    public List<Resena> obtenerResenasPorProducto(Long idProducto) {
        return resenaRepository.findByIdProducto(idProducto);
    }

    public Resena guardar(Resena resena) {
        return resenaRepository.save(resena);
    }

    public Optional<Resena> obtenerPorId(Long id) {
        return resenaRepository.findById(id);
    }

    public List<Resena> obtenerTodas() {
        return resenaRepository.findAll();
    }

    @Transactional
    public Resena actualizar(Long id, Resena resena) {
        Resena existente = resenaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Resena no encontrada"));
        existente.setCalificacion(resena.getCalificacion());
        existente.setComentario(resena.getComentario());
        return resenaRepository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        resenaRepository.deleteById(id);
    }
}
