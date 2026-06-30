package libreria.TiendaWeb.client;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InventarioClient {

    private static final String BASE_URL = "http://localhost:8083/api/v1";

    @Autowired
    private RestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarProductos() {
        try {
            return restTemplate.getForObject(BASE_URL + "/productos", List.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> buscarProductoPorId(Long idProducto) {
        try {
            return restTemplate.getForObject(
                BASE_URL + "/productos/" + idProducto, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean verificarStockSuficiente(Long idProducto, int cantidad) {
        try {
            Map<?, ?> response = restTemplate.getForObject(
                BASE_URL + "/productos/" + idProducto + "/stock?cantidad=" + cantidad,
                Map.class);
            return response != null && Boolean.TRUE.equals(response.get("suficiente"));
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> mostrarStockTotal(Long idProducto) {
        try {
            return restTemplate.getForObject(
                BASE_URL + "/productos/" + idProducto + "/stock", Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> reservarStock(Long idProducto, int cantidad, Long idOrden) {
        try {
            Map<String, Object> request = Map.of(
                "idProducto", idProducto,
                "cantidad", cantidad,
                "idOrden", idOrden);
            return restTemplate.postForObject(
                BASE_URL + "/reservas", request, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerPrecioYDetalle(Long idProducto) {
        try {
            return restTemplate.getForObject(
                BASE_URL + "/productos/" + idProducto + "/precio", Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}
