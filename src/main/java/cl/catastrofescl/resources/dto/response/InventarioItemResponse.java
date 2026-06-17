package cl.catastrofescl.resources.dto.response;

import cl.catastrofescl.resources.entity.EstadoCriticidad;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InventarioItemResponse(
        UUID id,
        UUID centroId,
        UUID itemCatalogoId,
        String itemNombre,
        String codigoCategoria,
        String nombreCategoria,
        String unidadMedida,
        long stockActual,
        long umbralMinimo,
        long umbralOptimo,
        long umbralMaximo,
        EstadoCriticidad estadoCriticidad,
        OffsetDateTime actualizadoEn
) {
}
