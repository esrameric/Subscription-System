package com.subscription.subscription.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT Token işlemleri için yardımcı sınıf.
 * customer-service ile aynı secret key kullanarak token doğrulaması yapar.
 */
@Component
public class JwtUtil {

    private final SecretKey key;

    public JwtUtil(@Value("${app.jwt.secret}") String secret) {
        // customer-service ile aynı default değeri kullan
        if (secret == null || secret.isBlank() || "change_me_in_env".equals(secret)) {
            secret = "default_change_me_secret_must_be_overridden_in_env_please_change";
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Token'dan kullanıcı adını (subject) çıkarır.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * Token'ın geçerli olup olmadığını kontrol eder.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            System.err.println("JWT Validation Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }
    }
}
