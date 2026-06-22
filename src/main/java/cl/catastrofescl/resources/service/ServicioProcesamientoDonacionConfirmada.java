package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.entity.EventoProcesado;
import cl.catastrofescl.resources.entity.TipoMovimiento;
import cl.catastrofescl.resources.event.DonacionConfirmadaEvento;
import cl.catastrofescl.resources.event.ItemMovimientoEventoDto;
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

/**
 * Procesa el evento {@code donation.confirmed} de ms-participacion-ciudadana sumando
 * los items donados al inventario del centro. ms-resources es la unica fuente de verdad del stock.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioProcesamientoDonacionConfirmada {

    private static final String PREFIJO_REDIS = "processed:donation.confirmed:";

    private final RepositorioEventosProcesados repositorioEventosProcesados;
    private final ServicioInventario servicioInventario;
    private final ObjectProvider<StringRedisTemplate> redisPlantilla;

    @Transactional
    public void procesar(DonacionConfirmadaEvento evento) {
        if (evento == null || evento.getEventId() == null) {
            log.warn("Evento donation.confirmed sin eventId, se ignora");
            return;
        }

        if (repositorioEventosProcesados.existsByEventoId(evento.getEventId())) {
            log.debug("Evento {} ya procesado (idempotencia BD)", evento.getEventId());
            return;
        }

        try {
            repositorioEventosProcesados.save(EventoProcesado.builder()
                    .eventoId(evento.getEventId())
                    .tipoEvento(DonacionConfirmadaEvento.ROUTING_KEY)
                    .build());
        } catch (DataIntegrityViolationException ex) {
            log.debug("Carrera al reservar donation.confirmed {}: {}", evento.getEventId(), ex.getMessage());
            return;
        }

        List<ItemMovimientoEventoDto> items = evento.getItems() != null
                ? evento.getItems()
                : Collections.emptyList();

        for (ItemMovimientoEventoDto item : items) {
            servicioInventario.registrarMovimientoInterno(
                    evento.getCentroId(),
                    item.getItemId(),
                    TipoMovimiento.INGRESO,
                    item.getCantidad(),
                    evento.getConfirmadoPorUsuarioId());
        }

        redisPlantilla.ifAvailable(redis ->
                redis.opsForValue().set(PREFIJO_REDIS + evento.getEventId(), "1", Duration.ofHours(24)));

        log.info("Cola ms-resources: procesado donation.confirmed eventId={} centro={} items={}",
                evento.getEventId(), evento.getCentroId(), items.size());
    }
}
