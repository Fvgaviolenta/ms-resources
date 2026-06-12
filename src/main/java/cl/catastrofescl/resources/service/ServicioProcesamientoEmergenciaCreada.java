package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.entity.EventoProcesado;
import cl.catastrofescl.resources.event.CentroAsociadoEnEventoDto;
import cl.catastrofescl.resources.event.EmergenciaCreadaEvento;
import cl.catastrofescl.resources.repository.RepositorioEventosProcesados;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioProcesamientoEmergenciaCreada {

    private static final String PREFIJO_REDIS = "processed:emergency.created:";

    private final RepositorioEventosProcesados repositorioEventosProcesados;
    private final ServicioCentros servicioCentros;
    private final ObjectProvider<StringRedisTemplate> redisPlantilla;

    @Transactional
    public void procesar(EmergenciaCreadaEvento evento) {
        if (evento == null || evento.getEventoId() == null) {
            log.warn("Evento emergency.created sin eventoId, se ignora");
            return;
        }

        if (repositorioEventosProcesados.existsByEventoId(evento.getEventoId())) {
            log.debug("Evento {} ya procesado (idempotencia BD)", evento.getEventoId());
            return;
        }

        try {
            repositorioEventosProcesados.save(EventoProcesado.builder()
                    .eventoId(evento.getEventoId())
                    .tipoEvento(EmergenciaCreadaEvento.ROUTING_KEY)
                    .build());
        } catch (DataIntegrityViolationException ex) {
            log.debug("Carrera al reservar procesamiento del evento {}: {}", evento.getEventoId(), ex.getMessage());
            return;
        }

        List<CentroAsociadoEnEventoDto> centros = evento.getCentrosAsociados() != null
                ? evento.getCentrosAsociados()
                : Collections.emptyList();

        for (CentroAsociadoEnEventoDto centro : centros) {
            servicioCentros.crearDesdeEvento(
                    evento.getEmergenciaId(),
                    evento.getDeclaradaPorUsuarioId(),
                    evento.getRegion(),
                    centro
            );
        }

        redisPlantilla.ifAvailable(redis -> {
            String clave = PREFIJO_REDIS + evento.getEventoId();
            redis.opsForValue().set(clave, "1", Duration.ofHours(24));
        });

        log.info("Cola ms-resources: procesado emergency.created eventoId={} emergenciaId={} centros={}",
                evento.getEventoId(), evento.getEmergenciaId(), centros.size());
    }
}
