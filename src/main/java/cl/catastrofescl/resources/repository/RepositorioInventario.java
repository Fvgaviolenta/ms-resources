package cl.catastrofescl.resources.repository;

import cl.catastrofescl.resources.entity.EstadoCriticidad;
import cl.catastrofescl.resources.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioInventario extends JpaRepository<Inventario, UUID> {

    List<Inventario> findByCentroIdOrderByItemCatalogoIdAsc(UUID centroId);

    Optional<Inventario> findByCentroIdAndItemCatalogoId(UUID centroId, UUID itemCatalogoId);

    @Query("""
            SELECT i FROM Inventario i
            INNER JOIN ItemCatalogo ic ON ic.id = i.itemCatalogoId
            INNER JOIN Categoria cat ON cat.id = ic.categoriaId
            WHERE i.estadoCriticidad IN :estados
            AND (:codigoCategoria IS NULL OR cat.codigo = :codigoCategoria)
            """)
    List<Inventario> buscarPorCriticidad(
            @Param("codigoCategoria") String codigoCategoria,
            @Param("estados") List<EstadoCriticidad> estados);

    long countByEstadoCriticidadIn(List<EstadoCriticidad> estados);

    @Query(value = """
            SELECT cat.codigo AS codigoCategoria,
                   cat.nombre AS nombreCategoria,
                   COALESCE(SUM(i.stock_actual), 0) AS stockTotal,
                   CASE
                       WHEN bool_or(i.estado_criticidad = 'AGOTADO') THEN 'AGOTADO'
                       WHEN bool_or(i.estado_criticidad = 'CRITICO') THEN 'CRITICO'
                       WHEN bool_or(i.estado_criticidad = 'SOBRESTOCK') THEN 'SOBRESTOCK'
                       WHEN bool_or(i.estado_criticidad = 'ABUNDANTE') THEN 'ABUNDANTE'
                       ELSE 'NORMAL'
                   END AS estadoCriticidadAgregado
            FROM inventario i
            INNER JOIN catalogo_items ci ON ci.id = i.item_catalogo_id
            INNER JOIN categorias cat ON cat.id = ci.categoria_id
            WHERE i.centro_id = :centroId
            GROUP BY cat.id, cat.codigo, cat.nombre, cat.orden
            ORDER BY cat.orden
            """, nativeQuery = true)
    List<ProyeccionResumenInventarioCategoria> resumenPorCategoria(@Param("centroId") UUID centroId);
}
