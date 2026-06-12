package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.dto.response.KpisInventarioResponse;
import cl.catastrofescl.resources.entity.EstadoCentro;
import cl.catastrofescl.resources.entity.EstadoCriticidad;
import cl.catastrofescl.resources.repository.RepositorioCentros;
import cl.catastrofescl.resources.repository.RepositorioInventario;
import cl.catastrofescl.resources.repository.RepositorioMovimientosInventario;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioKpis {

    public static final String CACHE_KPIS = "kpis";

    private final RepositorioCentros repositorioCentros;
    private final RepositorioInventario repositorioInventario;
    private final RepositorioMovimientosInventario repositorioMovimientosInventario;

    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_KPIS)
    public KpisInventarioResponse obtenerKpis() {
        long centrosActivos = repositorioCentros.countByEstado(EstadoCentro.ACTIVO);
        long criticos = repositorioInventario.countByEstadoCriticidadIn(
                List.of(EstadoCriticidad.CRITICO, EstadoCriticidad.AGOTADO));
        long sobrestock = repositorioInventario.countByEstadoCriticidadIn(
                List.of(EstadoCriticidad.SOBRESTOCK));
        long movimientos24h = repositorioMovimientosInventario.countByRegistradoEnAfter(
                OffsetDateTime.now().minusHours(24));

        return new KpisInventarioResponse(centrosActivos, criticos, sobrestock, movimientos24h);
    }
}
