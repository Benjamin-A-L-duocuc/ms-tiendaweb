package libreria.TiendaWeb.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LoginClient {

    private static final String BASE_URL = "http://localhost:8092/api/v1";

    @Autowired
    private RestTemplate restTemplate;

    public boolean validarSesion(Long idSesion) {
        try {
            Map<?, ?> response = restTemplate.getForObject(
                BASE_URL + "/sesion/" + idSesion, Map.class);
            return response != null && "Activa".equalsIgnoreCase((String) response.get("estado"));
        } catch (Exception e) {
            return false;
        }
    }

    public Map<?, ?> obtenerSesion(Long idSesion) {
        try {
            return restTemplate.getForObject(
                BASE_URL + "/sesion/" + idSesion, Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}
