package cl.catastrofescl.resources.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Item generico de un evento entrante que afecta el inventario (donacion confirmada o transferencia).
 * {@code itemId} corresponde al identificador del item de catalogo (catalogo_items.id) de ms-resources.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemMovimientoEventoDto {

    private UUID itemId;
    private long cantidad;
}
