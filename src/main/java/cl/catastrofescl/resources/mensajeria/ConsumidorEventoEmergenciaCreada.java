package cl.catastrofescl.resources.mensajeria;

import cl.catastrofescl.resources.event.EmergenciaCreadaEvento;
import cl.catastrofescl.resources.service.ServicioProcesamientoEmergenciaCreada;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumidorEventoEmergenciaCreada {

    private final ServicioProcesamientoEmergenciaCreada servicioProcesamiento;

    @RabbitListener(queues = "${catastrofescl.rabbitmq.cola-emergencia-creada}",
            messageConverter = "messageConverter")
    public void alRecibirEmergenciaCreada(EmergenciaCreadaEvento evento) {
        log.debug("ms-resources: emergency.created recibido emergenciaId={}", evento.getEmergenciaId());
        servicioProcesamiento.procesar(evento);
    }
}
