package cl.catastrofescl.resources.repository;

import cl.catastrofescl.resources.entity.EventoProcesado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RepositorioEventosProcesados extends JpaRepository<EventoProcesado, UUID> {

    boolean existsByEventoId(UUID eventoId);
}
