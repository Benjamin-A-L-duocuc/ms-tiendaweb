package libreria.TiendaWeb.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import libreria.TiendaWeb.model.Resena;
import libreria.TiendaWeb.repository.ResenaRepository;

@Service
public class ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;

    @Transactional
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

        existente.setIdUsuario(resena.getIdUsuario());
        existente.setCalificacion(resena.getCalificacion());
        existente.setComentario(resena.getComentario());
        existente.setFecha(resena.getFecha());

        return resenaRepository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        resenaRepository.deleteById(id);
    }
}
