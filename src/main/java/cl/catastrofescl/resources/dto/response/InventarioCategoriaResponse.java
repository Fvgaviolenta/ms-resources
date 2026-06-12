package cl.catastrofescl.resources.dto.response;

import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.entity.EstadoCriticidad;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InventarioCategoriaResponse(
        UUID id,
        UUID centroId,
        CategoriaInventario categoria,
        int stockActual,
        int umbralMinimo,
        int umbralOptimo,
        int umbralMaximo,
        EstadoCriticidad estadoCriticidad,
        OffsetDateTime actualizadoEn
) {
}
