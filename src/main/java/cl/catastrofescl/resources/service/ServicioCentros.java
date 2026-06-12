package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.dto.request.ActualizarCentroRequest;
import cl.catastrofescl.resources.dto.request.CrearCentroRequest;
import cl.catastrofescl.resources.dto.response.CentroResponse;
import cl.catastrofescl.resources.entity.Centro;
import cl.catastrofescl.resources.entity.EstadoCentro;
import cl.catastrofescl.resources.event.CentroAsociadoEnEventoDto;
import cl.catastrofescl.resources.exception.CentroNoEncontradoException;
import cl.catastrofescl.resources.repository.RepositorioCentros;
import cl.catastrofescl.resources.seguridad.ContextoUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioCentros {

    private final RepositorioCentros repositorioCentros;
    private final ServicioInventario servicioInventario;
    private final GeometriaMapper geometriaMapper;
    private final MapeadorCentros mapeadorCentros;
    private final ContextoUsuario contextoUsuario;

    @Transactional
    public CentroResponse crear(CrearCentroRequest solicitud) {
        UUID usuarioId = contextoUsuario.usuarioIdActual();
        Centro centro = Centro.builder()
                .nombre(solicitud.nombre())
                .direccion(solicitud.direccion())
                .coordenadas(geometriaMapper.aPunto(solicitud.coordenadas()))
                .region(solicitud.region())
                .comuna(solicitud.comuna())
                .capacidad(solicitud.capacidad())
                .horario(solicitud.horario())
                .estado(solicitud.estado() != null ? solicitud.estado() : EstadoCentro.ACTIVO)
                .creadoPorUsuarioId(usuarioId)
                .build();

        Centro persistido = repositorioCentros.save(centro);
        servicioInventario.inicializarInventarioCentro(persistido.getId());
        log.info("Centro creado id={} nombre={}", persistido.getId(), persistido.getNombre());
        return mapeadorCentros.aResponse(persistido);
    }

    @Transactional
    public CentroResponse crearDesdeEvento(UUID emergenciaId,
                                           UUID usuarioId,
                                           String region,
                                           CentroAsociadoEnEventoDto datos) {
        if (repositorioCentros.existsByEmergenciaIdAndNombre(emergenciaId, datos.getNombre())) {
            log.debug("Centro '{}' ya existe para emergencia {}, se omite duplicado",
                    datos.getNombre(), emergenciaId);
            return repositorioCentros.findByEmergenciaIdOrderByNombreAsc(emergenciaId).stream()
                    .filter(c -> c.getNombre().equals(datos.getNombre()))
                    .findFirst()
                    .map(mapeadorCentros::aResponse)
                    .orElseThrow();
        }

        Centro centro = Centro.builder()
                .nombre(datos.getNombre())
                .coordenadas(geometriaMapper.aPunto(datos.getLongitud(), datos.getLatitud()))
                .capacidad(datos.getCapacidadEstimada())
                .region(region)
                .estado(EstadoCentro.ACTIVO)
                .emergenciaId(emergenciaId)
                .creadoPorUsuarioId(usuarioId)
                .build();

        Centro persistido = repositorioCentros.save(centro);
        servicioInventario.inicializarInventarioCentro(persistido.getId());
        log.info("Centro desde evento creado id={} emergenciaId={} nombre={}",
                persistido.getId(), emergenciaId, persistido.getNombre());
        return mapeadorCentros.aResponse(persistido);
    }

    @Transactional(readOnly = true)
    public CentroResponse obtener(UUID id) {
        Centro centro = repositorioCentros.findById(id)
                .orElseThrow(() -> new CentroNoEncontradoException(id));
        return mapeadorCentros.aResponse(centro);
    }

    @Transactional(readOnly = true)
    public Page<CentroResponse> listar(UUID emergenciaId, int pagina, int tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Centro> paginaCentros = emergenciaId != null
                ? repositorioCentros.findByEmergenciaId(emergenciaId, pageable)
                : repositorioCentros.findAllByOrderByNombreAsc(pageable);
        return paginaCentros.map(mapeadorCentros::aResponse);
    }

    @Transactional(readOnly = true)
    public Page<CentroResponse> listarCercanos(double latitud, double longitud,
                                                 double radioMetros, int pagina, int tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano);
        return repositorioCentros.buscarCercanos(latitud, longitud, radioMetros, pageable)
                .map(mapeadorCentros::aResponse);
    }

    @Transactional
    public CentroResponse actualizar(UUID id, ActualizarCentroRequest solicitud) {
        Centro centro = repositorioCentros.findById(id)
                .orElseThrow(() -> new CentroNoEncontradoException(id));

        if (solicitud.nombre() != null) {
            centro.setNombre(solicitud.nombre());
        }
        if (solicitud.direccion() != null) {
            centro.setDireccion(solicitud.direccion());
        }
        if (solicitud.region() != null) {
            centro.setRegion(solicitud.region());
        }
        if (solicitud.comuna() != null) {
            centro.setComuna(solicitud.comuna());
        }
        if (solicitud.capacidad() != null) {
            centro.setCapacidad(solicitud.capacidad());
        }
        if (solicitud.horario() != null) {
            centro.setHorario(solicitud.horario());
        }
        if (solicitud.estado() != null) {
            centro.setEstado(solicitud.estado());
        }

        return mapeadorCentros.aResponse(repositorioCentros.save(centro));
    }
}
