package com.subscription.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Payment Service Application
 * 
 * Ödeme işlemlerini yöneten mikroservis
 * - Ödeme kayıtlarını yönetir
 * - Kafka ile subscription-service'e bildirim gönderir
 * - Müşteri ödemelerini takip eder
 */
@SpringBootApplication
@EnableJpaAuditing
public class PaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}

}
