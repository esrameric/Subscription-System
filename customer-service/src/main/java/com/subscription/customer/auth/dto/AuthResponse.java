package com.subscription.customer.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";

    /**
     * Tek parametreli constructor: Sadece token ile AuthResponse oluşturur.
     * tokenType otomatik olarak "Bearer" olur.
     * @param token JWT token string'i
     */
    public AuthResponse(String token) {
        this.token = token;
        this.tokenType = "Bearer";
    }
}
