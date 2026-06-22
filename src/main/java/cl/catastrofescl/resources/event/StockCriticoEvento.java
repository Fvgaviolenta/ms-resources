package cl.catastrofescl.resources.event;

import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.entity.EstadoCriticidad;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockCriticoEvento implements EventoDominio {

    public static final String ROUTING_KEY = "stock.critical";

    private UUID eventoId;
    private OffsetDateTime ocurridoEn;
    private String correlacionId;
    private String versionEvento;
    private String fuente;

    private UUID inventarioId;
    private UUID centroId;
    private UUID emergenciaId;
    private UUID itemCatalogoId;
    private CategoriaInventario categoria;
    private long stockActual;
    private long umbralMinimo;
    private long umbralOptimo;
    // Deficit hasta el nivel necesario (umbral optimo): cuanto falta donar para cubrir la necesidad
    private long cantidadSugerida;
    private EstadoCriticidad estadoCriticidad;

    @Override
    @JsonIgnore
    public String getRoutingKey() {
        return ROUTING_KEY;
    }
}
