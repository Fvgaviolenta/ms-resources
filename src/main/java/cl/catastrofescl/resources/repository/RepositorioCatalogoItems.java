package cl.catastrofescl.resources.repository;

import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.entity.ItemCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RepositorioCatalogoItems extends JpaRepository<ItemCatalogo, UUID> {

    List<ItemCatalogo> findByActivoTrueOrderByCategoriaAscNombreAsc();

    List<ItemCatalogo> findByCategoriaAndActivoTrueOrderByNombreAsc(CategoriaInventario categoria);
}
