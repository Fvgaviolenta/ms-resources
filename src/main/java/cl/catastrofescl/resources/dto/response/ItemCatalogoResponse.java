package cl.catastrofescl.resources.dto.response;

import cl.catastrofescl.resources.entity.CategoriaInventario;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ItemCatalogoResponse(
        UUID id,
        String nombre,
        CategoriaInventario categoria,
        String descripcion,
        String unidadMedida,
        boolean activo,
        OffsetDateTime creadoEn
) {
}
