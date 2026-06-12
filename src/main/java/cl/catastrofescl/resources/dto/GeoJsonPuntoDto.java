package cl.catastrofescl.resources.dto;

import java.util.List;

public record GeoJsonPuntoDto(String type, List<Double> coordinates) {
    public static GeoJsonPuntoDto de(double longitud, double latitud) {
        return new GeoJsonPuntoDto("Point", List.of(longitud, latitud));
    }
}
