package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.dto.response.CentroResponse;
import cl.catastrofescl.resources.entity.Centro;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MapeadorCentros {

    private final GeometriaMapper geometriaMapper;

    public CentroResponse aResponse(Centro centro) {
        return new CentroResponse(
                centro.getId(),
                centro.getNombre(),
                centro.getDireccion(),
                geometriaMapper.aPuntoGeoJson(centro.getCoordenadas()),
                centro.getRegion(),
                centro.getComuna(),
                centro.getCapacidad(),
                centro.getHorario(),
                centro.getEstado(),
                centro.getEmergenciaId(),
                centro.getCreadoPorUsuarioId(),
                centro.getCreadoEn(),
                centro.getActualizadoEn()
        );
    }
}
