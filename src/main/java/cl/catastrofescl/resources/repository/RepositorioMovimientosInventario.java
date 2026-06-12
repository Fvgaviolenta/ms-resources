package cl.catastrofescl.resources.repository;

import cl.catastrofescl.resources.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RepositorioMovimientosInventario extends JpaRepository<MovimientoInventario, UUID> {

    long countByRegistradoEnAfter(java.time.OffsetDateTime desde);
}
