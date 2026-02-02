package com.subscription.subscription.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security konfigürasyonu.
 * JWT tabanlı stateless authentication sağlar.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF devre dışı - stateless JWT kullanıyoruz
            .csrf(csrf -> csrf.disable())
            
            // Session yönetimi - stateless
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Endpoint yetkilendirme kuralları
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - kimlik doğrulama gerektirmez
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()
                
                // Offers endpoints - GET public, diğerleri authenticated
                .requestMatchers(HttpMethod.GET, "/api/v1/offers/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/offers").permitAll()
                
                // Subscriptions endpoints - authenticated gerektirir
                .requestMatchers("/api/v1/subscriptions/**").authenticated()
                
                // Diğer tüm istekler authenticated olmalı
                .anyRequest().authenticated()
            )
            
            // JWT filter'ı ekle
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
