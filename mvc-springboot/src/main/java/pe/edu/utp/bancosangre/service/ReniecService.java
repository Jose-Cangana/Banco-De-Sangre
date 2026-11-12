package pe.edu.utp.bancosangre.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class ReniecService {
    private final RestClient cliente;
    private final String token;

    public ReniecService(RestClient.Builder builder,
                         @Value("${app.reniec.url}") String url,
                         @Value("${app.reniec.token:}") String token) {
        this.cliente = builder.baseUrl(url).build();
        this.token = token.trim();
    }

    public Map<String, Object> consultar(String dni) {
        if (!dni.matches("\\d{8}")) throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos");
        if (token.isEmpty()) throw new IllegalStateException("La consulta RENIEC no está configurada: falta RENIEC_API_TOKEN");

        Map<?, ?> respuesta = cliente.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT, "application/json")
                .body(Map.of("dni", dni))
                .retrieve().body(Map.class);
        if (respuesta == null) throw new IllegalStateException("RENIEC devolvió una respuesta vacía");
        Object datosCrudos = respuesta.get("data");
        Map<?, ?> datos = datosCrudos instanceof Map<?, ?> mapa ? mapa : respuesta;
        // ApiPeru entrega nombre_completo con los apellidos primero. El sistema
        // debe mostrar el orden natural: nombres, apellido paterno y materno.
        String nombre = (valor(datos, "nombres") + " " + valor(datos, "apellido_paterno")
                + " " + valor(datos, "apellido_materno")).trim().replaceAll("\\s+", " ");
        if (nombre.isEmpty()) {
            nombre = valor(datos, "nombre_completo").trim();
        }
        if (nombre.isEmpty()) throw new IllegalStateException("DNI no encontrado");
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("dni", dni);
        resultado.put("nombre", nombre);
        String direccion = valor(datos, "direccion_completa").trim();
        if (direccion.isEmpty()) direccion = valor(datos, "direccion").trim();
        if (!direccion.isEmpty()) resultado.put("direccion", direccion);
        return resultado;
    }

    private String valor(Map<?, ?> respuesta, String campo) {
        Object valor = respuesta.get(campo);
        return valor == null ? "" : String.valueOf(valor);
    }
}
