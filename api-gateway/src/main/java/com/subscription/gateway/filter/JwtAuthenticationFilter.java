package com.subscription.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT Authentication Filter for API Gateway.
 * Gelen isteklerdeki JWT token'ları doğrular.
 * Spring Cloud Gateway'nin reactive GatewayFilter interface'ini implement eder.
 */
@Component
public class JwtAuthenticationFilter implements GatewayFilter {

    private final SecretKey key; // JWT imzalama için secret key

    /**
     * Constructor: JWT secret'i environment variable'dan alır.
     * @param secret JWT secret key (application.yml'den gelir)
     */
    public JwtAuthenticationFilter(@Value("${app.jwt.secret:change_me_in_env}") String secret) {
        if (secret == null || secret.isBlank() || "change_me_in_env".equals(secret)) {
            secret = "default_change_me_secret_must_be_overridden_in_env_please_change";
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gateway filter method'u.
     * Her istek için çalışır ve JWT token doğrulama yapar.
     * @param exchange ServerWebExchange: HTTP request/response wrapper
     * @param chain GatewayFilterChain: Sonraki filter'lara geçmek için
     * @return Mono<Void>: Reactive response
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Authorization header'ını al
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Header yoksa veya Bearer token değilse, 401 Unauthorized dön
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange);
        }

        // "Bearer " kısmını çıkar ve token'ı al
        String token = authHeader.substring(7);

        // Token'ı doğrula
        if (!validateToken(token)) {
            return unauthorizedResponse(exchange);
        }

        // Token geçerli ise, username'i header'a ekle (downstream servisler için)
        String username = getUsernameFromToken(token);
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Authenticated-User", username) // Özel header ekle
                .build();

        // İsteği değiştirilmiş request ile devam ettir
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/customer/auth/");
    }

    /**
     * JWT token'ı doğrular.
     * @param token JWT token string'i
     * @return boolean: Token geçerli mi?
     */


    private boolean validateToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) key)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getUsernameFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Claims claims = Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }


    /**
     * 401 Unauthorized response döner.
     * @param exchange ServerWebExchange
     * @return Mono<Void>: Unauthorized response
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED); // 401 status kodu
        return response.setComplete(); // Response'u tamamla
    }
}
