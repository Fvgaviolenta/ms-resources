package cl.catastrofescl.resources.repository;

import cl.catastrofescl.resources.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioCategorias extends JpaRepository<Categoria, UUID> {

    List<Categoria> findByActivoTrueOrderByOrdenAsc();

    Optional<Categoria> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, UUID id);
}
