package cl.catastrofescl.resources.dto.response;

import cl.catastrofescl.resources.dto.GeoJsonPuntoDto;
import cl.catastrofescl.resources.entity.EstadoCentro;

import java.util.UUID;

public record PuntoMapaCentroResponse(
        String type,
        GeoJsonPuntoDto geometry,
        PropiedadesMapaCentroResponse properties
) {
    public static PuntoMapaCentroResponse de(
            UUID id,
            String nombre,
            String region,
            EstadoCentro estado,
            double longitud,
            double latitud,
            String criticidadMaxima) {
        return new PuntoMapaCentroResponse(
                "Feature",
                GeoJsonPuntoDto.de(longitud, latitud),
                new PropiedadesMapaCentroResponse(id, nombre, region, estado, criticidadMaxima)
        );
    }
}
