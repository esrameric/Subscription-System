package com.subscription.gateway.config;

import com.subscription.gateway.filter.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway konfigürasyon sınıfı.
 * Mikroservis route'larını ve global filter'ları tanımlar.
 * @Configuration: Spring konfigürasyon sınıfı olarak işaretler.
 */
@Configuration
public class GatewayConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public GatewayConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Route tanımları.
     * Farklı path'leri ilgili mikroservislere yönlendirir.
     * @param builder RouteLocatorBuilder: Gateway route'larını oluşturmak için
     * @return RouteLocator: Tanımlanmış route'lar
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Customer service route'u: /api/customer/** path'lerini customer-service'e yönlendir
                .route("customer-service", r -> r
                        .path("/api/customer/**") // Gelen istek path'i
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter) // JWT doğrulama filter'ı
                                .rewritePath("/api/customer/(?<segment>.*)", "/api/v1/${segment}")
                        )
                        .uri("http://localhost:8081") // Hedef servis URL'i
                )
                // Diğer servisler için route'lar eklenebilir (subscription-service, payment-service vb.)
                .build();
    }
}
