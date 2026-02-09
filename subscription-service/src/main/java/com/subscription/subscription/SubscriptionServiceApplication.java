package com.subscription.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Subscription Service - Ana Uygulama Sınıfı
 * 
 * Bu sınıf Spring Boot uygulamasının başlangıç noktasıdır.
 * 
 * @SpringBootApplication: 3 önemli annotation'ı birleştirir:
 *   1. @Configuration: Bu sınıfın Spring configuration sınıfı olduğunu belirtir
 *   2. @EnableAutoConfiguration: Spring Boot'un otomatik konfigürasyonunu aktif eder
 *   3. @ComponentScan: Bu package ve alt package'lardaki @Component'leri tarar
 * 
 * @EnableScheduling: Zamanlanmış görevleri (scheduled tasks) aktif eder
 *   - @Scheduled annotation'lı methodların çalışmasını sağlar
 *   - SubscriptionRenewalJob için gerekli
 *   - Cron expression'lar ve fixed rate/delay desteklenir
 * 
 * @EnableFeignClients: OpenFeign REST client'ları aktif eder
 *   - Payment Service ile iletişim için kullanılır
 *   - Declarative REST client (interface-based)
 * 
 * @EnableCaching: Spring Cache abstraction'ı aktif eder
 *   - @Cacheable, @CacheEvict annotation'larını etkinleştirir
 *   - Redis ile entegre çalışır
 */
@SpringBootApplication
@EnableScheduling  // Zamanlanmış görevler için (Subscription renewal job)
@EnableFeignClients  // Feign REST client'ları için
@EnableCaching  // Cache desteği için (Redis ile)
public class SubscriptionServiceApplication {

	/**
	 * Main Method - Uygulamanın başlangıç noktası
	 * 
	 * SpringApplication.run():
	 * - Spring IoC Container'ı başlatır
	 * - Embedded Tomcat server'ı çalıştırır
	 * - Auto-configuration'ları uygular
	 * - Bean'leri oluşturur ve dependency injection yapar
	 * 
	 * @param args Komut satırı argümanları (örn: --spring.profiles.active=dev)
	 */
	public static void main(String[] args) {
		SpringApplication.run(SubscriptionServiceApplication.class, args);
	}

}
