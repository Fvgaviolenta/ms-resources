package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.dto.response.ColeccionMapaCentrosResponse;
import cl.catastrofescl.resources.dto.response.PuntoMapaCentroResponse;
import cl.catastrofescl.resources.entity.EstadoCentro;
import cl.catastrofescl.resources.repository.ProyeccionMapaCentro;
import cl.catastrofescl.resources.repository.RepositorioCentros;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioMapData {

    public static final String CACHE_MAP_DATA = "map-data";

    private final RepositorioCentros repositorioCentros;

    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_MAP_DATA, unless = "#result.features().isEmpty()")
    public ColeccionMapaCentrosResponse obtenerDatosMapa() {
        var features = repositorioCentros.listarDatosMapa().stream()
                .map(this::aFeature)
                .toList();
        return ColeccionMapaCentrosResponse.de(features);
    }

    private PuntoMapaCentroResponse aFeature(ProyeccionMapaCentro proyeccion) {
        return PuntoMapaCentroResponse.de(
                proyeccion.getId(),
                proyeccion.getNombre(),
                proyeccion.getRegion(),
                EstadoCentro.valueOf(proyeccion.getEstado()),
                proyeccion.getLongitud(),
                proyeccion.getLatitud(),
                proyeccion.getCriticidadMaxima()
        );
    }
}
