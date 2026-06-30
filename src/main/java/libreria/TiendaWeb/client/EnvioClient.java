package libreria.TiendaWeb.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EnvioClient {

    private static final String BASE_URL = "http://localhost:8084/api/v1";

    @Autowired
    private RestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    public Map<String, Object> buscarEnvioPorOrden(Long idOrden) {
        try {
            return restTemplate.getForObject(
                BASE_URL + "/envios/orden/" + idOrden, Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}
