package cl.catastrofescl.resources.dto.response;

import java.util.List;

public record ColeccionMapaCentrosResponse(
        String type,
        List<PuntoMapaCentroResponse> features
) {
    public static ColeccionMapaCentrosResponse de(List<PuntoMapaCentroResponse> features) {
        return new ColeccionMapaCentrosResponse("FeatureCollection", features);
    }
}
