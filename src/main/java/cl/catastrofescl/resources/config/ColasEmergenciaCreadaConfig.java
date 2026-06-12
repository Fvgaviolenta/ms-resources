package cl.catastrofescl.resources.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ColasEmergenciaCreadaConfig {

    @Value("${catastrofescl.rabbitmq.cola-emergencia-creada}")
    private String colaPrincipal;

    @Value("${catastrofescl.rabbitmq.cola-emergencia-creada-dlq}")
    private String colaDlq;

    @Bean
    public Queue colaEmergenciaCreadaResources() {
        return QueueBuilder.durable(colaPrincipal)
                .withArgument("x-dead-letter-exchange", "catastrofescl.dlx")
                .withArgument("x-dead-letter-routing-key", colaDlq)
                .build();
    }

    @Bean
    public Queue colaEmergenciaCreadaResourcesDlq() {
        return QueueBuilder.durable(colaDlq).build();
    }

    @Bean
    public Binding bindingEmergenciaCreadaResourcesDlq(Queue colaEmergenciaCreadaResourcesDlq,
                                                       DirectExchange catastrofesclDlxExchange) {
        return BindingBuilder.bind(colaEmergenciaCreadaResourcesDlq)
                .to(catastrofesclDlxExchange)
                .with(colaDlq);
    }
}
