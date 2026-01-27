package com.subscription.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway ana uygulama sınıfı.
 * Spring Cloud Gateway kullanarak mikroservisler arası routing ve authentication sağlar.
 * @SpringBootApplication: Spring Boot uygulamasını başlatır ve auto-configuration etkinleştirir.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}