package cl.catastrofescl.resources.dto.response;

import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.entity.EstadoCriticidad;

import java.util.UUID;

public record SugerenciaRedistribucionResponse(
        CategoriaInventario categoria,
        UUID itemCatalogoId,
        String itemNombre,
        UUID centroOrigenId,
        String centroOrigenNombre,
        long stockOrigen,
        EstadoCriticidad criticidadOrigen,
        UUID centroDestinoId,
        String centroDestinoNombre,
        long stockDestino,
        EstadoCriticidad criticidadDestino,
        double distanciaMetros,
        long cantidadSugerida
) {
}
