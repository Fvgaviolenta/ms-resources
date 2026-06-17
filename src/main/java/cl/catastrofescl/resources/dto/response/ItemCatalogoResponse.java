package cl.catastrofescl.resources.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ItemCatalogoResponse(
        UUID id,
        String nombre,
        UUID categoriaId,
        String codigoCategoria,
        String nombreCategoria,
        String descripcion,
        String unidadMedida,
        boolean activo,
        OffsetDateTime creadoEn
) {
}
