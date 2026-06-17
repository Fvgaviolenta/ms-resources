package cl.catastrofescl.resources.dto.response;

import cl.catastrofescl.resources.entity.EstadoCriticidad;

public record ResumenInventarioCategoriaResponse(
        String codigoCategoria,
        String nombreCategoria,
        long stockTotal,
        EstadoCriticidad estadoCriticidadAgregado
) {
}
