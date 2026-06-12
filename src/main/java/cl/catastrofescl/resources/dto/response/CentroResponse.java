package cl.catastrofescl.resources.dto.response;

import cl.catastrofescl.resources.dto.GeoJsonPuntoDto;
import cl.catastrofescl.resources.entity.EstadoCentro;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CentroResponse(
        UUID id,
        String nombre,
        String direccion,
        GeoJsonPuntoDto coordenadas,
        String region,
        String comuna,
        Integer capacidad,
        String horario,
        EstadoCentro estado,
        UUID emergenciaId,
        UUID creadoPorUsuarioId,
        OffsetDateTime creadoEn,
        OffsetDateTime actualizadoEn
) {
}
