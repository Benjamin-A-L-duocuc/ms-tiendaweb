package libreria.TiendaWeb.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import libreria.TiendaWeb.client.LoginClient;
import libreria.TiendaWeb.model.Orden;
import libreria.TiendaWeb.model.Resena;
import libreria.TiendaWeb.model.enums.EstadoOrden;
import libreria.TiendaWeb.repository.OrdenRepository;
import libreria.TiendaWeb.repository.ResenaRepository;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private OrdenRepository ordenRepository;

    @Mock
    private LoginClient loginClient;

    @InjectMocks
    private ResenaService resenaService;

    @Test
    void reseñarProducto_conExito() {
        Long idSesion = 1L;
        when(loginClient.validarSesion(idSesion)).thenReturn(true);
        when(loginClient.obtenerSesion(idSesion)).thenReturn((Map) Map.of("idUsuario", 10L));

        Resena input = new Resena(null, null, 100L, 5, "Excelente libro, muy recomendado para todos", null);

        Orden orden = new Orden();
        orden.setEstado(EstadoOrden.ENTREGADA);
        when(ordenRepository.findByIdUsuario(10L)).thenReturn(List.of(orden));
        when(resenaRepository.findByIdUsuarioAndIdProducto(10L, 100L)).thenReturn(Optional.empty());

        Resena saved = new Resena(1L, 10L, 100L, 5, "Excelente libro, muy recomendado para todos", LocalDateTime.now());
        when(resenaRepository.save(any())).thenReturn(saved);

        Resena result = resenaService.reseñarProducto(input, idSesion);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getIdUsuario()).isEqualTo(10L);
        verify(resenaRepository).save(any());
    }

    @Test
    void reseñarProducto_sinCompra_lanzaExcepcion() {
        Long idSesion = 1L;
        when(loginClient.validarSesion(idSesion)).thenReturn(true);
        when(loginClient.obtenerSesion(idSesion)).thenReturn((Map) Map.of("idUsuario", 10L));

        Resena input = new Resena(null, null, 100L, 5, "Excelente libro, muy recomendado para todos", null);

        when(ordenRepository.findByIdUsuario(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> resenaService.reseñarProducto(input, idSesion))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Debe haber comprado");
    }

    @Test
    void reseñarProducto_duplicado_lanzaExcepcion() {
        Long idSesion = 1L;
        when(loginClient.validarSesion(idSesion)).thenReturn(true);
        when(loginClient.obtenerSesion(idSesion)).thenReturn((Map) Map.of("idUsuario", 10L));

        Resena input = new Resena(null, null, 100L, 5, "Excelente libro, muy recomendado para todos", null);

        Orden orden = new Orden();
        orden.setEstado(EstadoOrden.ENTREGADA);
        when(ordenRepository.findByIdUsuario(10L)).thenReturn(List.of(orden));
        when(resenaRepository.findByIdUsuarioAndIdProducto(10L, 100L)).thenReturn(Optional.of(new Resena()));

        assertThatThrownBy(() -> resenaService.reseñarProducto(input, idSesion))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Ya ha reseñado");
    }

    @Test
    void reseñarProducto_comentarioCorto_lanzaExcepcion() {
        Long idSesion = 1L;
        when(loginClient.validarSesion(idSesion)).thenReturn(true);
        when(loginClient.obtenerSesion(idSesion)).thenReturn((Map) Map.of("idUsuario", 10L));

        Resena input = new Resena(null, null, 100L, 5, "Corto", null);

        Orden orden = new Orden();
        orden.setEstado(EstadoOrden.ENTREGADA);
        when(ordenRepository.findByIdUsuario(10L)).thenReturn(List.of(orden));
        when(resenaRepository.findByIdUsuarioAndIdProducto(10L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resenaService.reseñarProducto(input, idSesion))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("20 y 1000 caracteres");
    }

    @Test
    void reseñarProducto_palabraProhibida_lanzaExcepcion() {
        Long idSesion = 1L;
        when(loginClient.validarSesion(idSesion)).thenReturn(true);
        when(loginClient.obtenerSesion(idSesion)).thenReturn((Map) Map.of("idUsuario", 10L));

        Resena input = new Resena(null, null, 100L, 5, "Este producto es una estafa total", null);

        Orden orden = new Orden();
        orden.setEstado(EstadoOrden.ENTREGADA);
        when(ordenRepository.findByIdUsuario(10L)).thenReturn(List.of(orden));
        when(resenaRepository.findByIdUsuarioAndIdProducto(10L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resenaService.reseñarProducto(input, idSesion))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("palabras prohibidas");
    }

    @Test
    void cambiarResena_conExito() {
        Long idSesion = 1L;
        when(loginClient.validarSesion(idSesion)).thenReturn(true);
        when(loginClient.obtenerSesion(idSesion)).thenReturn((Map) Map.of("idUsuario", 10L));

        Resena existente = new Resena(1L, 10L, 100L, 3, "Texto anterior", LocalDateTime.now());
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(existente));

        Resena input = new Resena(1L, null, null, null, "Nuevo comentario excelente y muy detallado", null);

        Resena updated = new Resena(1L, 10L, 100L, 3, "Nuevo comentario excelente y muy detallado", LocalDateTime.now());
        when(resenaRepository.save(any())).thenReturn(updated);

        Resena result = resenaService.cambiarResena(input, idSesion);

        assertThat(result).isNotNull();
        assertThat(result.getComentario()).isEqualTo("Nuevo comentario excelente y muy detallado");
    }

    @Test
    void cambiarResena_noPropietario_lanzaExcepcion() {
        Long idSesion = 1L;
        when(loginClient.validarSesion(idSesion)).thenReturn(true);
        when(loginClient.obtenerSesion(idSesion)).thenReturn((Map) Map.of("idUsuario", 99L));

        Resena existente = new Resena(1L, 10L, 100L, 3, "Texto anterior", LocalDateTime.now());
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(existente));

        Resena input = new Resena(1L, null, null, null, "Nuevo comentario excelente y muy detallado", null);

        assertThatThrownBy(() -> resenaService.cambiarResena(input, idSesion))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("No puede editar");
    }

    @Test
    void obtenerResenasPorProducto() {
        when(resenaRepository.findByIdProducto(100L)).thenReturn(List.of(new Resena()));
        List<Resena> result = resenaService.obtenerResenasPorProducto(100L);
        assertThat(result).hasSize(1);
        verify(resenaRepository).findByIdProducto(100L);
    }

    @Test
    void guardar() {
        Resena resena = new Resena();
        when(resenaRepository.save(resena)).thenReturn(resena);
        assertThat(resenaService.guardar(resena)).isSameAs(resena);
    }

    @Test
    void obtenerPorId() {
        Resena resena = new Resena();
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));
        assertThat(resenaService.obtenerPorId(1L)).containsSame(resena);
    }

    @Test
    void obtenerTodas() {
        when(resenaRepository.findAll()).thenReturn(List.of(new Resena()));
        assertThat(resenaService.obtenerTodas()).hasSize(1);
    }

    @Test
    void actualizar() {
        Resena existente = new Resena(1L, 10L, 100L, 3, "Antes", LocalDateTime.now());
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(existente));

        Resena cambios = new Resena();
        cambios.setCalificacion(5);
        cambios.setComentario("Despues");

        Resena merged = new Resena(1L, 10L, 100L, 5, "Despues", LocalDateTime.now());
        when(resenaRepository.save(any())).thenReturn(merged);

        Resena result = resenaService.actualizar(1L, cambios);
        assertThat(result.getCalificacion()).isEqualTo(5);
        assertThat(result.getComentario()).isEqualTo("Despues");
    }

    @Test
    void eliminar() {
        resenaService.eliminar(1L);
        verify(resenaRepository).deleteById(1L);
    }
}
