package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.event.EventoDominio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PublicadorEventos {

    private static final String VERSION_EVENTO = "1.0";
    private static final String FUENTE_EVENTO = "ms-resources";

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeTopic;

    public PublicadorEventos(RabbitTemplate rabbitTemplate,
                             @Value("${catastrofescl.rabbitmq.exchange-topic}") String exchangeTopic) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeTopic = exchangeTopic;
    }

    public void publicar(EventoDominio evento) {
        log.debug("Publicando evento {} routingKey={}", evento.getClass().getSimpleName(), evento.getRoutingKey());
        rabbitTemplate.convertAndSend(exchangeTopic, evento.getRoutingKey(), evento, mensaje -> {
            mensaje.getMessageProperties().setMessageId(evento.getEventoId().toString());
            mensaje.getMessageProperties().setCorrelationId(evento.getCorrelacionId());
            mensaje.getMessageProperties().setContentType("application/json");
            return mensaje;
        });
    }

    public static String versionEvento() {
        return VERSION_EVENTO;
    }

    public static String fuenteEvento() {
        return FUENTE_EVENTO;
    }
}
