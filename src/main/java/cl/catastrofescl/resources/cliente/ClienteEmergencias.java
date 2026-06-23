package cl.catastrofescl.resources.cliente;

import cl.catastrofescl.resources.exception.EmergenciaNoValidaException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Cliente HTTP hacia ms-emergencies para validar emergencias al asociarlas a un centro.
 *
 * <p>Reenvia las credenciales del request actual (token Firebase via Authorization y, en
 * modo dev, las cabeceras X-Dev-*) para que ms-emergencies autorice la consulta.
 */
@Slf4j
@Component
public class ClienteEmergencias {

    private static final String ESTADO_ACTIVA = "ACTIVA";

    private final RestClient restClient;

    public ClienteEmergencias(
            @Value("${catastrofescl.emergencies.base-url:http://ms-emergencies:8082}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * Verifica que la emergencia exista y este ACTIVA. Lanza {@link EmergenciaNoValidaException}
     * si no existe, no esta activa o no pudo validarse.
     */
    public void validarActiva(UUID emergenciaId) {
        EmergenciaRemota emergencia;
        try {
            emergencia = restClient.get()
                    .uri("/emergencies/{id}", emergenciaId)
                    .headers(this::propagarAutenticacion)
                    .retrieve()
                    .body(EmergenciaRemota.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new EmergenciaNoValidaException(emergenciaId, "La emergencia indicada no existe");
        } catch (RestClientException ex) {
            log.warn("No fue posible validar la emergencia {} contra ms-emergencies: {}",
                    emergenciaId, ex.getMessage());
            throw new EmergenciaNoValidaException(emergenciaId,
                    "No fue posible validar la emergencia con el servicio de emergencias");
        }

        if (emergencia == null || emergencia.estado() == null) {
            throw new EmergenciaNoValidaException(emergenciaId, "No fue posible validar la emergencia");
        }
        if (!ESTADO_ACTIVA.equalsIgnoreCase(emergencia.estado())) {
            throw new EmergenciaNoValidaException(emergenciaId,
                    "La emergencia no esta activa (estado actual: " + emergencia.estado() + ")");
        }
    }

    private void propagarAutenticacion(HttpHeaders headers) {
        ServletRequestAttributes atributos =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (atributos == null) {
            return;
        }
        HttpServletRequest request = atributos.getRequest();
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
        copiarCabecera(request, headers, "X-Dev-User-Id");
        copiarCabecera(request, headers, "X-Dev-Roles");
        copiarCabecera(request, headers, "X-Dev-Permissions");
    }

    private void copiarCabecera(HttpServletRequest request, HttpHeaders headers, String nombre) {
        String valor = request.getHeader(nombre);
        if (valor != null && !valor.isBlank()) {
            headers.set(nombre, valor);
        }
    }

    /** Proyeccion minima de la respuesta de ms-emergencies (solo lo necesario para validar). */
    record EmergenciaRemota(UUID id, String estado) {
    }
}
