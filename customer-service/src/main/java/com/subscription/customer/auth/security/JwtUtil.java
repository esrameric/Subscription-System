package com.subscription.customer.auth.security;

// Import statements for JWT library and Spring components
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm; // Removed - deprecated in newer JJWT versions
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Component annotation makes this a Spring-managed bean
@Component
public class JwtUtil {

    // Secret key for signing JWT tokens
    private final SecretKey key;
    // Token expiration time in milliseconds
    private final long expirationMs;

    // Constructor with dependency injection for JWT configuration
    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-ms}") long expirationMs) {
        // Validate and set default secret if not properly configured
        if (secret == null || secret.isBlank() || "change_me_in_env".equals(secret)) {
            secret = "default_change_me_secret_must_be_overridden_in_env_please_change";
        }
        // Generate HMAC-SHA key from secret string
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // Generate a new JWT token for the given username
    public String generateToken(String username) {
        // Current timestamp
        Date now = new Date();
        // Calculate expiration date
        Date expiry = new Date(now.getTime() + expirationMs);

        // Build and sign the JWT token
        return Jwts.builder()
                .subject(username)     // Set the username as subject (fluent API)
                .issuedAt(now)         // Set token issuance time (fluent API)
                .expiration(expiry)    // Set token expiration time (fluent API)
                .signWith(key)         // Sign with the secret key (algorithm auto-detected)
                .compact();  // Build the compact JWT string
    }

    
    public String getUsernameFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Claims claims = Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
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
        /**
         * JWT token'ı doğrular.
         * @param token JWT token string'i
         * @return boolean: Token geçerli mi?
         */
}