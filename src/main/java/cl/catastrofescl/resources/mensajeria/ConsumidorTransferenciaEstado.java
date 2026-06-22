package cl.catastrofescl.resources.mensajeria;

import cl.catastrofescl.resources.event.TransferenciaEstadoCambiadaEvento;
import cl.catastrofescl.resources.service.ServicioProcesamientoTransferenciaEstado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumidorTransferenciaEstado {

    private final ServicioProcesamientoTransferenciaEstado servicioProcesamiento;

    @RabbitListener(queues = "${catastrofescl.rabbitmq.cola-transferencia-estado}",
            messageConverter = "messageConverter")
    public void alRecibirTransferenciaEstado(TransferenciaEstadoCambiadaEvento evento) {
        log.debug("ms-resources: transfer.status.changed recibido transferenciaId={}",
                evento != null ? evento.getTransferenciaId() : null);
        servicioProcesamiento.procesar(evento);
    }
}
