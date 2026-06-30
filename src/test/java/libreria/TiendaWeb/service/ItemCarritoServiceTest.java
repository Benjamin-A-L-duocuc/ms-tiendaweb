package libreria.TiendaWeb.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import libreria.TiendaWeb.model.ItemCarrito;
import libreria.TiendaWeb.repository.ItemCarritoRepository;

@ExtendWith(MockitoExtension.class)
class ItemCarritoServiceTest {

    @Mock
    private ItemCarritoRepository itemCarritoRepository;

    @InjectMocks
    private ItemCarritoService itemCarritoService;

    @Test
    void obtenerPorCarritoId() {
        when(itemCarritoRepository.findByCarritoId(1L)).thenReturn(List.of(new ItemCarrito()));
        assertThat(itemCarritoService.obtenerPorCarritoId(1L)).hasSize(1);
    }

    @Test
    void guardar() {
        ItemCarrito item = new ItemCarrito();
        when(itemCarritoRepository.save(item)).thenReturn(item);
        assertThat(itemCarritoService.guardar(item)).isSameAs(item);
    }

    @Test
    void obtenerPorId() {
        ItemCarrito item = new ItemCarrito();
        when(itemCarritoRepository.findById(1L)).thenReturn(Optional.of(item));
        assertThat(itemCarritoService.obtenerPorId(1L)).containsSame(item);
    }

    @Test
    void obtenerTodos() {
        when(itemCarritoRepository.findAll()).thenReturn(List.of(new ItemCarrito()));
        assertThat(itemCarritoService.obtenerTodos()).hasSize(1);
    }

    @Test
    void actualizar() {
        ItemCarrito existente = new ItemCarrito(1L, 100L, "Libro", 1, 15.0, null);
        when(itemCarritoRepository.findById(1L)).thenReturn(Optional.of(existente));

        ItemCarrito cambios = new ItemCarrito();
        cambios.setCantidad(3);
        cambios.setIdProducto(200L);
        cambios.setNombreProducto("Otro Libro");
        cambios.setPrecioUnitario(20.0);

        ItemCarrito merged = new ItemCarrito(1L, 200L, "Otro Libro", 3, 20.0, null);
        when(itemCarritoRepository.save(any())).thenReturn(merged);

        ItemCarrito result = itemCarritoService.actualizar(1L, cambios);
        assertThat(result.getCantidad()).isEqualTo(3);
        assertThat(result.getIdProducto()).isEqualTo(200L);
        assertThat(result.getNombreProducto()).isEqualTo("Otro Libro");
        assertThat(result.getPrecioUnitario()).isEqualTo(20.0);
    }

    @Test
    void eliminar() {
        itemCarritoService.eliminar(1L);
        verify(itemCarritoRepository).deleteById(1L);
    }
}
