package cl.catastrofescl.resources.dto.response;

import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.entity.EstadoCriticidad;

import java.util.UUID;

public record SugerenciaRedistribucionResponse(
        CategoriaInventario categoria,
        UUID centroOrigenId,
        String centroOrigenNombre,
        int stockOrigen,
        EstadoCriticidad criticidadOrigen,
        UUID centroDestinoId,
        String centroDestinoNombre,
        int stockDestino,
        EstadoCriticidad criticidadDestino,
        double distanciaMetros,
        int cantidadSugerida
) {
}
