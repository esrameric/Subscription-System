package com.subscription.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server - Service Discovery
 * 
 * Mikroservislerin birbirini bulmasını sağlar:
 * - Her servis başladığında Eureka'ya kayıt olur
 * - Eureka tüm servislerin listesini tutar
 * - Servisler birbirini isimle çağırabilir (IP:Port yerine)
 * 
 * Dashboard: http://localhost:8761
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaServerApplication.class, args);
	}

}
