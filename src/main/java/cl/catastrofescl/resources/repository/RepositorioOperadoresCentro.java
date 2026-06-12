package cl.catastrofescl.resources.repository;

import cl.catastrofescl.resources.entity.OperadorCentro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioOperadoresCentro extends JpaRepository<OperadorCentro, UUID> {

    List<OperadorCentro> findByCentroIdOrderByAsignadoEnDesc(UUID centroId);

    boolean existsByCentroIdAndUsuarioId(UUID centroId, UUID usuarioId);

    Optional<OperadorCentro> findByCentroIdAndUsuarioId(UUID centroId, UUID usuarioId);
}
