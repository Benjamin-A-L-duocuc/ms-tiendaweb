package libreria.TiendaWeb.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VentasClient {

    private static final String BASE_URL = "http://localhost:8087/api/v1";

    @Autowired
    private RestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    public Map<String, Object> buscarDescuentoPorId(Long idDescuento) {
        try {
            return restTemplate.getForObject(
                BASE_URL + "/descuentos/" + idDescuento, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> crearVentaOnline(Map<String, Object> request) {
        try {
            return restTemplate.postForObject(
                BASE_URL + "/ventas/online", request, Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}
