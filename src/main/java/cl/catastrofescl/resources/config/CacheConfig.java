package cl.catastrofescl.resources.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

import cl.catastrofescl.resources.service.ServicioCatalogoItems;
import cl.catastrofescl.resources.service.ServicioKpis;
import cl.catastrofescl.resources.service.ServicioMapData;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Personaliza el {@code RedisCacheManager} autoconfigurado por Spring Boot en lugar de
     * declarar un bean propio con {@code @ConditionalOnBean} (anti-patron sensible al orden que
     * provocaba que se usara el serializador JDK por defecto). Asi los valores se serializan como
     * JSON y los DTO (records) no necesitan implementar {@code Serializable}.
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return builder -> builder
                .cacheDefaults(base.entryTtl(Duration.ofSeconds(60)))
                .withCacheConfiguration(ServicioMapData.CACHE_MAP_DATA, base.entryTtl(Duration.ofSeconds(60)))
                .withCacheConfiguration(ServicioKpis.CACHE_KPIS, base.entryTtl(Duration.ofSeconds(30)))
                .withCacheConfiguration(ServicioCatalogoItems.CACHE_CATALOGO, base.entryTtl(Duration.ofSeconds(300)));
    }
}
