package cl.catastrofescl.resources.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CategoriaResponse(
        UUID id,
        String codigo,
        String nombre,
        boolean activo,
        int orden,
        OffsetDateTime creadoEn
) {
}
