package cl.catastrofescl.resources.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Configuracion de Redis para cache y almacen de idempotencia.
 * En esta fase el MS solo usa StringRedisTemplate; cuando se agreguen consumidores
 * se anadira el RedisTemplate generico para el set `processed:{eventId}`.
 *
 * <p>El bean se declara como condicional para no romper entornos donde Redis
 * este deshabilitado (por ejemplo, tests de integracion).</p>
 */
@Configuration
public class RedisConfig {

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}

