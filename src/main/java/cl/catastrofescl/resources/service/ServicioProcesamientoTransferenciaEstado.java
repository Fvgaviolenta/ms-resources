package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.entity.EventoProcesado;
import cl.catastrofescl.resources.entity.TipoMovimiento;
import cl.catastrofescl.resources.event.ItemMovimientoEventoDto;
import cl.catastrofescl.resources.event.TransferenciaEstadoCambiadaEvento;
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
 * Procesa el evento {@code transfer.status.changed} de ms-logistica. Cuando la transferencia pasa a
 * RECIBIDA aplica EGRESO en el centro origen y, si el destino es otro centro, INGRESO en el destino.
 * Para entregas a la comunidad / punto de distribucion (tipoDestino != CENTRO) solo se aplica el EGRESO.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioProcesamientoTransferenciaEstado {

    private static final String PREFIJO_REDIS = "processed:transfer.status.changed:";
    private static final String DESTINO_CENTRO = "CENTRO";

    private final RepositorioEventosProcesados repositorioEventosProcesados;
    private final ServicioInventario servicioInventario;
    private final ObjectProvider<StringRedisTemplate> redisPlantilla;

    @Transactional
    public void procesar(TransferenciaEstadoCambiadaEvento evento) {
        if (evento == null || evento.getEventoId() == null) {
            log.warn("Evento transfer.status.changed sin eventoId, se ignora");
            return;
        }

        if (!TransferenciaEstadoCambiadaEvento.ESTADO_RECIBIDA.equalsIgnoreCase(evento.getEstadoNuevo())) {
            log.debug("transfer.status.changed estado={} no aplica movimiento de stock", evento.getEstadoNuevo());
            return;
        }

        if (repositorioEventosProcesados.existsByEventoId(evento.getEventoId())) {
            log.debug("Evento {} ya procesado (idempotencia BD)", evento.getEventoId());
            return;
        }

        try {
            repositorioEventosProcesados.save(EventoProcesado.builder()
                    .eventoId(evento.getEventoId())
                    .tipoEvento(TransferenciaEstadoCambiadaEvento.ROUTING_KEY)
                    .build());
        } catch (DataIntegrityViolationException ex) {
            log.debug("Carrera al reservar transfer.status.changed {}: {}", evento.getEventoId(), ex.getMessage());
            return;
        }

        boolean haciaCentro = esDestinoCentro(evento) && evento.getCentroDestinoId() != null;
        List<ItemMovimientoEventoDto> items = evento.getItems() != null
                ? evento.getItems()
                : Collections.emptyList();

        for (ItemMovimientoEventoDto item : items) {
            servicioInventario.registrarMovimientoInterno(
                    evento.getCentroOrigenId(),
                    item.getItemId(),
                    TipoMovimiento.EGRESO,
                    item.getCantidad(),
                    null);

            if (haciaCentro) {
                servicioInventario.registrarMovimientoInterno(
                        evento.getCentroDestinoId(),
                        item.getItemId(),
                        TipoMovimiento.INGRESO,
                        item.getCantidad(),
                        null);
            }
        }

        redisPlantilla.ifAvailable(redis ->
                redis.opsForValue().set(PREFIJO_REDIS + evento.getEventoId(), "1", Duration.ofHours(24)));

        log.info("Cola ms-resources: procesado transfer.status.changed eventoId={} origen={} destino={} tipoDestino={} items={}",
                evento.getEventoId(), evento.getCentroOrigenId(), evento.getCentroDestinoId(),
                evento.getTipoDestino(), items.size());
    }

    private boolean esDestinoCentro(TransferenciaEstadoCambiadaEvento evento) {
        return evento.getTipoDestino() == null || DESTINO_CENTRO.equalsIgnoreCase(evento.getTipoDestino());
    }
}
