package cl.catastrofescl.resources.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Colas y bindings de los eventos entrantes que mutan el inventario:
 * {@code donation.confirmed} (ingreso por donacion) y {@code transfer.status.changed} (egreso/ingreso por transferencia).
 * Cada cola tiene su DLQ siguiendo el estandar del proyecto.
 */
@Configuration
public class ColasInventarioEventosConfig {

    @Value("${catastrofescl.rabbitmq.cola-donacion-confirmada}")
    private String colaDonacion;

    @Value("${catastrofescl.rabbitmq.cola-donacion-confirmada-dlq}")
    private String colaDonacionDlq;

    @Value("${catastrofescl.rabbitmq.cola-transferencia-estado}")
    private String colaTransferencia;

    @Value("${catastrofescl.rabbitmq.cola-transferencia-estado-dlq}")
    private String colaTransferenciaDlq;

    // ----- donation.confirmed -----

    @Bean
    public Queue colaDonacionConfirmadaResources() {
        return QueueBuilder.durable(colaDonacion)
                .withArgument("x-dead-letter-exchange", "catastrofescl.dlx")
                .withArgument("x-dead-letter-routing-key", colaDonacionDlq)
                .build();
    }

    @Bean
    public Queue colaDonacionConfirmadaResourcesDlq() {
        return QueueBuilder.durable(colaDonacionDlq).build();
    }

    @Bean
    public Binding bindingDonacionConfirmadaTopic(Queue colaDonacionConfirmadaResources,
                                                  TopicExchange catastrofesclEventsExchange) {
        return BindingBuilder.bind(colaDonacionConfirmadaResources)
                .to(catastrofesclEventsExchange)
                .with("donation.confirmed");
    }

    @Bean
    public Binding bindingDonacionConfirmadaResourcesDlq(Queue colaDonacionConfirmadaResourcesDlq,
                                                         DirectExchange catastrofesclDlxExchange) {
        return BindingBuilder.bind(colaDonacionConfirmadaResourcesDlq)
                .to(catastrofesclDlxExchange)
                .with(colaDonacionDlq);
    }

    // ----- transfer.status.changed -----

    @Bean
    public Queue colaTransferenciaEstadoResources() {
        return QueueBuilder.durable(colaTransferencia)
                .withArgument("x-dead-letter-exchange", "catastrofescl.dlx")
                .withArgument("x-dead-letter-routing-key", colaTransferenciaDlq)
                .build();
    }

    @Bean
    public Queue colaTransferenciaEstadoResourcesDlq() {
        return QueueBuilder.durable(colaTransferenciaDlq).build();
    }

    @Bean
    public Binding bindingTransferenciaEstadoTopic(Queue colaTransferenciaEstadoResources,
                                                   TopicExchange catastrofesclEventsExchange) {
        return BindingBuilder.bind(colaTransferenciaEstadoResources)
                .to(catastrofesclEventsExchange)
                .with("transfer.status.changed");
    }

    @Bean
    public Binding bindingTransferenciaEstadoResourcesDlq(Queue colaTransferenciaEstadoResourcesDlq,
                                                          DirectExchange catastrofesclDlxExchange) {
        return BindingBuilder.bind(colaTransferenciaEstadoResourcesDlq)
                .to(catastrofesclDlxExchange)
                .with(colaTransferenciaDlq);
    }
}
