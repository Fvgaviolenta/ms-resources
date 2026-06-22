package cl.catastrofescl.resources.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Espejo local del evento {@code transfer.status.changed} publicado por ms-logistica.
 * Cuando {@code estadoNuevo == RECIBIDA}, ms-resources aplica EGRESO en el centro origen y,
 * si el destino es un centro, INGRESO en el centro destino. Si {@code tipoDestino != CENTRO}
 * (entrega a la comunidad / punto de distribucion) solo aplica el EGRESO definitivo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferenciaEstadoCambiadaEvento {

    public static final String ROUTING_KEY = "transfer.status.changed";
    public static final String ESTADO_RECIBIDA = "RECIBIDA";

    private UUID eventoId;
    private OffsetDateTime ocurridoEn;
    private String correlacionId;
    private UUID transferenciaId;
    private String estadoAnterior;
    private String estadoNuevo;
    private UUID centroOrigenId;
    private UUID centroDestinoId;
    // CENTRO | PUNTO_DISTRIBUCION | COMUNIDAD
    private String tipoDestino;
    private List<ItemMovimientoEventoDto> items;
}
