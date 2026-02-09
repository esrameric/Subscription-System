package com.subscription.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Notification Service Ana Uygulama Sınıfı
 * 
 * Bu servis bildirim yönetiminden sorumludur:
 * - Kafka'dan payment eventlerini dinler
 * - Email, SMS ve Push bildirimleri gönderir
 * - Bildirim geçmişini saklar
 * 
 * @EnableKafka: Kafka consumer işlevselliğini aktif eder
 * @SpringBootApplication: Spring Boot otomatik yapılandırmasını sağlar
 */
@SpringBootApplication
@EnableKafka
public class NotificationServiceApplication {

	/**
	 * Uygulamayı başlatır
	 * Port: 8084
	 */
	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}
}
