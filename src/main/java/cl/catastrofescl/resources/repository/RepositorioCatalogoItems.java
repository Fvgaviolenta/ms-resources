package cl.catastrofescl.resources.repository;

import cl.catastrofescl.resources.entity.ItemCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioCatalogoItems extends JpaRepository<ItemCatalogo, UUID> {

    List<ItemCatalogo> findByActivoTrueOrderByNombreAsc();

    List<ItemCatalogo> findByCategoriaIdAndActivoTrueOrderByNombreAsc(UUID categoriaId);

    Optional<ItemCatalogo> findByIdAndActivoTrue(UUID id);

    List<ItemCatalogo> findByIdIn(List<UUID> ids);
}
