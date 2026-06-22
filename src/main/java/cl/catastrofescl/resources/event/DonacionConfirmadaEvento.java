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
 * Espejo local del evento {@code donation.confirmed} publicado por ms-participacion-ciudadana.
 * Al recibirlo, ms-resources suma (INGRESO) al inventario del centro los items donados.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DonacionConfirmadaEvento {

    public static final String ROUTING_KEY = "donation.confirmed";

    private UUID eventId;
    private UUID donacionId;
    private UUID centroId;
    private UUID confirmadoPorUsuarioId;
    private String codigoQr;
    private List<ItemMovimientoEventoDto> items;
    private OffsetDateTime confirmadoEn;
}
