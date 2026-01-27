package com.subscription.customer.auth.service.impl;

// Import statements for DTOs, models, repositories, and utilities
import com.subscription.customer.auth.dto.AuthResponse;
import com.subscription.customer.auth.dto.LoginRequest;
import com.subscription.customer.auth.dto.RegisterRequest;
import com.subscription.customer.auth.model.User;
import com.subscription.customer.auth.repository.UserRepository;
import com.subscription.customer.auth.service.AuthService;
import com.subscription.customer.auth.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// Service annotation marks this class as a Spring service component
@Service
public class AuthServiceImpl implements AuthService {

    // Dependency injection fields for required components
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // Constructor for dependency injection
    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    // Register method implementation with transaction management
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email is already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Create new user with encoded password and default settings
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles("ROLE_USER")
                .status("ACTIVE")
                .build();

        // Save user to database
        userRepository.save(user);

        // Generate JWT token for the new user
        String token = jwtUtil.generateToken(user.getEmail());

        // Return authentication response with token
        return new AuthResponse(token);
    }

    // Authentication method for user login
    @Override
    public AuthResponse authenticate(LoginRequest request) {
        // Authenticate user credentials using Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token for authenticated user
        String token = jwtUtil.generateToken(request.getEmail());

        // Return authentication response with token
        return new AuthResponse(token);
    }
}
