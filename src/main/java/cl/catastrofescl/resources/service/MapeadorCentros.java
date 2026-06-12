package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.dto.response.CentroResponse;
import cl.catastrofescl.resources.dto.response.InventarioCategoriaResponse;
import cl.catastrofescl.resources.entity.Centro;
import cl.catastrofescl.resources.entity.Inventario;
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

    public InventarioCategoriaResponse aInventarioResponse(Inventario inventario) {
        return new InventarioCategoriaResponse(
                inventario.getId(),
                inventario.getCentroId(),
                inventario.getCategoria(),
                inventario.getStockActual(),
                inventario.getUmbralMinimo(),
                inventario.getUmbralOptimo(),
                inventario.getUmbralMaximo(),
                inventario.getEstadoCriticidad(),
                inventario.getActualizadoEn()
        );
    }
}
