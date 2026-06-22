package cl.catastrofescl.resources.mensajeria;

import cl.catastrofescl.resources.event.DonacionConfirmadaEvento;
import cl.catastrofescl.resources.service.ServicioProcesamientoDonacionConfirmada;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumidorDonacionConfirmada {

    private final ServicioProcesamientoDonacionConfirmada servicioProcesamiento;

    @RabbitListener(queues = "${catastrofescl.rabbitmq.cola-donacion-confirmada}",
            messageConverter = "messageConverter")
    public void alRecibirDonacionConfirmada(DonacionConfirmadaEvento evento) {
        log.debug("ms-resources: donation.confirmed recibido centroId={}",
                evento != null ? evento.getCentroId() : null);
        servicioProcesamiento.procesar(evento);
    }
}
