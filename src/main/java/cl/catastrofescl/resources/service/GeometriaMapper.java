package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.dto.CoordenadaDto;
import cl.catastrofescl.resources.dto.GeoJsonPuntoDto;
import cl.catastrofescl.resources.exception.GeometriaInvalidaException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
public class GeometriaMapper {

    public static final int SRID_WGS84 = 4326;

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), SRID_WGS84);

    public Point aPunto(CoordenadaDto coordenada) {
        if (coordenada == null) {
            throw new GeometriaInvalidaException("Las coordenadas son obligatorias");
        }
        Point punto = geometryFactory.createPoint(
                new Coordinate(coordenada.longitud(), coordenada.latitud()));
        punto.setSRID(SRID_WGS84);
        return punto;
    }

    public Point aPunto(double longitud, double latitud) {
        return aPunto(new CoordenadaDto(longitud, latitud));
    }

    public GeoJsonPuntoDto aPuntoGeoJson(Point punto) {
        if (punto == null) {
            return null;
        }
        return GeoJsonPuntoDto.de(punto.getX(), punto.getY());
    }
}
