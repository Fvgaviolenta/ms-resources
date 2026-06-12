package cl.catastrofescl.resources.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OperadorCentroResponse(
        UUID id,
        UUID centroId,
        UUID usuarioId,
        UUID asignadoPorUsuarioId,
        OffsetDateTime asignadoEn
) {
}
