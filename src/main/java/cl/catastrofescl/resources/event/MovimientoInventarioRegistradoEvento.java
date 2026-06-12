package cl.catastrofescl.resources.event;

import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.entity.EstadoCriticidad;
import cl.catastrofescl.resources.entity.TipoMovimiento;
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
public class MovimientoInventarioRegistradoEvento implements EventoDominio {

    public static final String ROUTING_KEY = "inventory.movement.registered";

    private UUID eventoId;
    private OffsetDateTime ocurridoEn;
    private String correlacionId;
    private String versionEvento;
    private String fuente;

    private UUID movimientoId;
    private UUID inventarioId;
    private UUID centroId;
    private CategoriaInventario categoria;
    private TipoMovimiento tipoMovimiento;
    private int cantidad;
    private int stockAnterior;
    private int stockPosterior;
    private EstadoCriticidad estadoCriticidadPosterior;
    private UUID registradoPorUsuarioId;

    @Override
    @JsonIgnore
    public String getRoutingKey() {
        return ROUTING_KEY;
    }
}
