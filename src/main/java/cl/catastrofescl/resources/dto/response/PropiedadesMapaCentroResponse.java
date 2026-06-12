package cl.catastrofescl.resources.dto.response;

import cl.catastrofescl.resources.entity.EstadoCentro;

import java.util.UUID;

public record PropiedadesMapaCentroResponse(
        UUID id,
        String nombre,
        String region,
        EstadoCentro estado,
        String criticidadMaxima
) {
}
