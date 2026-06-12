package cl.catastrofescl.resources.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BindingEmergenciaCreadaConfig {

    @Value("${catastrofescl.rabbitmq.cola-emergencia-creada}")
    private String colaPrincipal;

    @Bean
    public Binding bindingEmergenciaCreadaTopic(Queue colaEmergenciaCreadaResources,
                                                TopicExchange catastrofesclEventsExchange) {
        return BindingBuilder.bind(colaEmergenciaCreadaResources)
                .to(catastrofesclEventsExchange)
                .with("emergency.created");
    }
}
