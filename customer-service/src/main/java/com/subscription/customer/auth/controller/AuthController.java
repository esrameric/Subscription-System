package com.subscription.customer.auth.controller;

import com.subscription.customer.auth.dto.AuthResponse;
import com.subscription.customer.auth.dto.LoginRequest;
import com.subscription.customer.auth.dto.RegisterRequest;
import com.subscription.customer.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse resp = authService.register(request);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse resp = authService.authenticate(request);
        return ResponseEntity.ok(resp);
    }
}
