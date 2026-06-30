package libreria.TiendaWeb.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RegistroUsuarioClient {

    private static final String BASE_URL = "http://localhost:8093/api/v1";

    @Autowired
    private RestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    public Map<String, Object> consultarUsuario(Long idUsuario) {
        try {
            return restTemplate.getForObject(
                BASE_URL + "/usuarios/" + idUsuario, Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}
