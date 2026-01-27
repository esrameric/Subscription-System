package com.subscription.customer.auth.service;

import com.subscription.customer.auth.dto.AuthResponse;
import com.subscription.customer.auth.dto.LoginRequest;
import com.subscription.customer.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse authenticate(LoginRequest request);
}
