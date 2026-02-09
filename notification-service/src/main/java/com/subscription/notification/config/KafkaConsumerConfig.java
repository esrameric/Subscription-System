package com.subscription.notification.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer Yapılandırması
 * 
 * Kafka'dan mesaj tüketimi için gerekli bean'leri yapılandırır:
 * - ConsumerFactory: Temel Kafka consumer ayarları
 * - KafkaListenerContainerFactory: @KafkaListener için container yapılandırması
 * 
 * Önemli Özellikler:
 * - Manuel Acknowledgment: Mesaj başarıyla işlenmedikçe commit edilmez
 * - JSON Deserialization: Gelen mesajlar otomatik olarak Java objesine çevrilir
 * - Earliest Offset: İlk kez başlatıldığında topic'in başından okur
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;  // Kafka broker adresi (localhost:9092)

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;  // Consumer group ID (notification-service-group)

    /**
     * Kafka Consumer Factory Bean
     * 
     * Consumer'ın temel yapılandırmasını yapar:
     * - BOOTSTRAP_SERVERS: Kafka broker adresi
     * - GROUP_ID: Aynı gruptaki consumerlar mesajları paylaşır
     * - KEY_DESERIALIZER: Key'i String'e çevirir
     * - VALUE_DESERIALIZER: Value'yu JSON'dan Java objesine çevirir
     * - TRUSTED_PACKAGES: Güvenlik için, tüm paketlerden deserialize edilebilir (*)
     * - AUTO_OFFSET_RESET: İlk başlatmada topic'in başından oku (earliest)
     * - ENABLE_AUTO_COMMIT: false - Manuel olarak acknowledge edeceğiz
     * 
     * @return ConsumerFactory instance
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");  // Güvenlik: tüm paketlere izin ver
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");  // Baştan oku
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);  // Manuel commit
        
        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Kafka Listener Container Factory Bean
     * 
     * @KafkaListener annotation'u için container yapılandırması
     * 
     * Manuel Acknowledgment modunda çalışır:
     * - Mesaj başarıyla işlendiğinde acknowledgment.acknowledge() çağrılır
     * - Acknowledge edilmeyen mesajlar tekrar işlenir
     * - Bu sayede at-least-once delivery garanti edilir
     * 
     * @return KafkaListenerContainerFactory instance
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // MANUAL mod: acknowledgment.acknowledge() çağrılana kadar commit etme
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
