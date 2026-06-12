package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.dto.request.AsignarOperadorCentroRequest;
import cl.catastrofescl.resources.dto.response.OperadorCentroResponse;
import cl.catastrofescl.resources.entity.OperadorCentro;
import cl.catastrofescl.resources.exception.CentroNoEncontradoException;
import cl.catastrofescl.resources.exception.OperadorYaAsignadoException;
import cl.catastrofescl.resources.repository.RepositorioCentros;
import cl.catastrofescl.resources.repository.RepositorioOperadoresCentro;
import cl.catastrofescl.resources.seguridad.ContextoUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioOperadoresCentro {

    private final RepositorioOperadoresCentro repositorioOperadoresCentro;
    private final RepositorioCentros repositorioCentros;
    private final ContextoUsuario contextoUsuario;

    @Transactional
    public OperadorCentroResponse asignar(UUID centroId, AsignarOperadorCentroRequest solicitud) {
        verificarCentroExiste(centroId);
        if (repositorioOperadoresCentro.existsByCentroIdAndUsuarioId(centroId, solicitud.usuarioId())) {
            throw new OperadorYaAsignadoException(centroId, solicitud.usuarioId());
        }

        UUID asignadorId = contextoUsuario.usuarioIdActual();
        OperadorCentro operador = OperadorCentro.builder()
                .centroId(centroId)
                .usuarioId(solicitud.usuarioId())
                .asignadoPorUsuarioId(asignadorId)
                .build();

        OperadorCentro persistido = repositorioOperadoresCentro.save(operador);
        log.info("Operador {} asignado al centro {} por {}", solicitud.usuarioId(), centroId, asignadorId);
        return aResponse(persistido);
    }

    @Transactional(readOnly = true)
    public List<OperadorCentroResponse> listarPorCentro(UUID centroId) {
        verificarCentroExiste(centroId);
        return repositorioOperadoresCentro.findByCentroIdOrderByAsignadoEnDesc(centroId).stream()
                .map(this::aResponse)
                .toList();
    }

    private void verificarCentroExiste(UUID centroId) {
        if (!repositorioCentros.existsById(centroId)) {
            throw new CentroNoEncontradoException(centroId);
        }
    }

    private OperadorCentroResponse aResponse(OperadorCentro operador) {
        return new OperadorCentroResponse(
                operador.getId(),
                operador.getCentroId(),
                operador.getUsuarioId(),
                operador.getAsignadoPorUsuarioId(),
                operador.getAsignadoEn()
        );
    }
}
