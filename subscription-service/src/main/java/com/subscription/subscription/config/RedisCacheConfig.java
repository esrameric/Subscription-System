package com.subscription.subscription.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis Cache Configuration
 * 
 * Spring Cache ile Redis entegrasyonunu yapılandırır.
 * JSON serializasyon ve TTL ayarları bu sınıfta tanımlanır.
 * 
 * Özellikler:
 * - JSON formatında veri saklama (okunabilir ve debug-friendly)
 * - Configurable TTL (varsayılan 6 saat)
 * - Type information ile güvenli deserialization
 */
@Configuration
public class RedisCacheConfig implements CachingConfigurer {

    /**
     * Cache TTL değeri (saniye cinsinden)
     * 
     * Environment variable ile override edilebilir: CACHE_TTL_SECONDS
     * Varsayılan: 21600 saniye (6 saat)
     */
    @Value("${cache.ttl-seconds:21600}")
    private long cacheTtlSeconds;

    /**
     * Redis CacheManager Bean
     * 
     * Spring'in @Cacheable, @CacheEvict gibi annotation'ları
     * bu CacheManager'ı kullanarak Redis'te veri saklar.
     * 
     * @param connectionFactory Redis bağlantı factory (auto-configured)
     * @return Konfigüre edilmiş RedisCacheManager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // JSON serializer için ObjectMapper yapılandırması
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Java 8+ DateTime API desteği (Instant, LocalDateTime, vb.)
        objectMapper.registerModule(new JavaTimeModule());
        
        // Type information'ı JSON'a dahil et (deserialization için gerekli)
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // JSON serializer oluştur
        GenericJackson2JsonRedisSerializer jsonSerializer = 
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Redis cache konfigürasyonu
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                // TTL ayarı - cache'deki verilerin ne kadar süre saklanacağı
                .entryTtl(Duration.ofSeconds(cacheTtlSeconds))
                // Null değerleri cache'leme (performans için)
                .disableCachingNullValues()
                // Key serializer: String olarak sakla
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                // Value serializer: JSON olarak sakla
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                jsonSerializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .transactionAware()
                .build();
    }
}
