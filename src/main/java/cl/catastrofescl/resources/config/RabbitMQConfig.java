package cl.catastrofescl.resources.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara el topic exchange principal y el dead letter exchange del sistema.
 * Los consumidores de eventos viven en los otros microservicios.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${catastrofescl.rabbitmq.exchange-topic}")
    private String exchangeTopic;

    @Value("${catastrofescl.rabbitmq.exchange-dlx}")
    private String exchangeDlx;

    @Bean
    public TopicExchange catastrofesclEventsExchange() {
        return new TopicExchange(exchangeTopic, true, false);
    }

    @Bean
    public DirectExchange catastrofesclDlxExchange() {
        return new DirectExchange(exchangeDlx, true, false);
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(exchangeTopic);
        return template;
    }
}

