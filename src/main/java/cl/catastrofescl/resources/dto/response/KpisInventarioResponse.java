package cl.catastrofescl.resources.dto.response;

public record KpisInventarioResponse(
        long centrosActivos,
        long itemsCriticosOAgotados,
        long itemsSobrestock,
        long movimientosUltimas24h
) {
}
