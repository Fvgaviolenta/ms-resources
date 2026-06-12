package cl.catastrofescl.resources.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmergenciaCreadaEvento implements EventoDominio {

    public static final String ROUTING_KEY = "emergency.created";

    private UUID eventoId;
    private OffsetDateTime ocurridoEn;
    private String correlacionId;
    private String versionEvento;
    private String fuente;

    private UUID emergenciaId;
    private String tipo;
    private String severidad;
    private String estado;
    private String region;
    private String resumen;
    private UUID declaradaPorUsuarioId;

    private List<CentroAsociadoEnEventoDto> centrosAsociados;

    @Override
    @JsonIgnore
    public String getRoutingKey() {
        return ROUTING_KEY;
    }
}
