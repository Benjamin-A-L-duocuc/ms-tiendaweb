package libreria.TiendaWeb.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import libreria.TiendaWeb.model.Orden;
import libreria.TiendaWeb.repository.OrdenRepository;

@Service
public class OrdenService {

    @Autowired
    private OrdenRepository ordenRepository;

    @Transactional
    public Orden guardar(Orden orden) {
        return ordenRepository.save(orden);
    }

    public Optional<Orden> obtenerPorId(Long id) {
        return ordenRepository.findById(id);
    }

    public List<Orden> obtenerTodas() {
        return ordenRepository.findAll();
    }

    @Transactional
    public Orden actualizar(Long id, Orden orden) {
        Orden existente = ordenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        existente.setIdUsuario(orden.getIdUsuario());
        existente.setFechaOrden(orden.getFechaOrden());
        existente.setSubtotal(orden.getSubtotal());
        existente.setImpuestos(orden.getImpuestos());
        existente.setTotal(orden.getTotal());
        existente.setMedioPago(orden.getMedioPago());
        existente.setEstado(orden.getEstado());
        existente.setDireccionEnvio(orden.getDireccionEnvio());
        existente.setDireccionFacturacion(orden.getDireccionFacturacion());

        return ordenRepository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        ordenRepository.deleteById(id);
    }
}
