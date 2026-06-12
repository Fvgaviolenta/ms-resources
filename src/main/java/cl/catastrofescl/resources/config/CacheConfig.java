package cl.catastrofescl.resources.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

import cl.catastrofescl.resources.service.ServicioCatalogoItems;
import cl.catastrofescl.resources.service.ServicioKpis;
import cl.catastrofescl.resources.service.ServicioMapData;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> porCache = Map.of(
                ServicioMapData.CACHE_MAP_DATA, base.entryTtl(Duration.ofSeconds(60)),
                ServicioKpis.CACHE_KPIS, base.entryTtl(Duration.ofSeconds(30)),
                ServicioCatalogoItems.CACHE_CATALOGO, base.entryTtl(Duration.ofSeconds(300))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(base.entryTtl(Duration.ofSeconds(60)))
                .withInitialCacheConfigurations(porCache)
                .build();
    }
}
