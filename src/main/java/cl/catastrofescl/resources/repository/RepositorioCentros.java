package cl.catastrofescl.resources.repository;

import cl.catastrofescl.resources.entity.Centro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RepositorioCentros extends JpaRepository<Centro, UUID> {

    Page<Centro> findByEmergenciaId(UUID emergenciaId, Pageable pageable);

    Page<Centro> findAllByOrderByNombreAsc(Pageable pageable);

    boolean existsByEmergenciaIdAndNombre(UUID emergenciaId, String nombre);

    @Query(value = """
            SELECT c.* FROM centros c
            WHERE ST_DWithin(
                c.coordenadas::geography,
                ST_SetSRID(ST_MakePoint(:longitud, :latitud), 4326)::geography,
                :radioMetros
            )
            ORDER BY ST_Distance(
                c.coordenadas::geography,
                ST_SetSRID(ST_MakePoint(:longitud, :latitud), 4326)::geography
            )
            """,
            countQuery = """
            SELECT COUNT(*) FROM centros c
            WHERE ST_DWithin(
                c.coordenadas::geography,
                ST_SetSRID(ST_MakePoint(:longitud, :latitud), 4326)::geography,
                :radioMetros
            )
            """,
            nativeQuery = true)
    Page<Centro> buscarCercanos(@Param("latitud") double latitud,
                                @Param("longitud") double longitud,
                                @Param("radioMetros") double radioMetros,
                                Pageable pageable);

    List<Centro> findByEmergenciaIdOrderByNombreAsc(UUID emergenciaId);

    long countByEstado(cl.catastrofescl.resources.entity.EstadoCentro estado);

    @Query(value = """
            SELECT c.id AS id,
                   c.nombre AS nombre,
                   c.region AS region,
                   c.estado AS estado,
                   ST_X(c.coordenadas::geometry) AS longitud,
                   ST_Y(c.coordenadas::geometry) AS latitud,
                   COALESCE(
                       (SELECT CASE
                           WHEN bool_or(i.estado_criticidad = 'AGOTADO') THEN 'AGOTADO'
                           WHEN bool_or(i.estado_criticidad = 'CRITICO') THEN 'CRITICO'
                           WHEN bool_or(i.estado_criticidad = 'SOBRESTOCK') THEN 'SOBRESTOCK'
                           WHEN bool_or(i.estado_criticidad = 'ABUNDANTE') THEN 'ABUNDANTE'
                           ELSE 'NORMAL'
                       END
                       FROM inventario i WHERE i.centro_id = c.id),
                       'AGOTADO'
                   ) AS criticidadMaxima
            FROM centros c
            WHERE c.estado = 'ACTIVO'
            ORDER BY c.nombre
            """, nativeQuery = true)
    List<ProyeccionMapaCentro> listarDatosMapa();

    @Query(value = """
            SELECT ST_Distance(
                (SELECT coordenadas FROM centros WHERE id = :centroA)::geography,
                (SELECT coordenadas FROM centros WHERE id = :centroB)::geography
            )
            """, nativeQuery = true)
    Double distanciaMetrosEntreCentros(@Param("centroA") UUID centroA, @Param("centroB") UUID centroB);
}
