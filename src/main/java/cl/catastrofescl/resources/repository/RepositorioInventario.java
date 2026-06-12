package cl.catastrofescl.resources.repository;

import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.entity.EstadoCriticidad;
import cl.catastrofescl.resources.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioInventario extends JpaRepository<Inventario, UUID> {

    List<Inventario> findByCentroIdOrderByCategoriaAsc(UUID centroId);

    Optional<Inventario> findByCentroIdAndCategoria(UUID centroId, CategoriaInventario categoria);

    List<Inventario> findByCategoriaAndEstadoCriticidadIn(
            CategoriaInventario categoria, List<EstadoCriticidad> estados);

    @Query("""
            SELECT i FROM Inventario i
            WHERE i.estadoCriticidad IN :estados
            AND (:categoria IS NULL OR i.categoria = :categoria)
            """)
    List<Inventario> buscarPorCriticidad(
            @Param("categoria") CategoriaInventario categoria,
            @Param("estados") List<EstadoCriticidad> estados);

    long countByEstadoCriticidadIn(List<EstadoCriticidad> estados);
}
