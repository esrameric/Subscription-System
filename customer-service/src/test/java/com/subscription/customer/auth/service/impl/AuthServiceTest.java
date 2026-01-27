package com.subscription.customer.auth.service.impl;

import com.subscription.customer.auth.dto.AuthResponse;
import com.subscription.customer.auth.dto.LoginRequest;
import com.subscription.customer.auth.dto.RegisterRequest;
import com.subscription.customer.auth.model.User;
import com.subscription.customer.auth.repository.UserRepository;
import com.subscription.customer.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AuthServiceImpl için birim testleri.
 * Mockito kullanarak bağımlılıkları mock eder ve sadece servis mantığını test eder.
 */
@ExtendWith(MockitoExtension.class) // Mockito'nun JUnit 5 entegrasyonu için gerekli
class AuthServiceTest {

    @Mock
    private UserRepository userRepository; // @Mock: UserRepository'nin mock versiyonu

    @Mock
    private PasswordEncoder passwordEncoder; // @Mock: PasswordEncoder'ın mock'u (BCryptPasswordEncoder)

    @Mock
    private JwtUtil jwtUtil; // @Mock: JwtUtil'in mock'u (JWT token işlemleri için)

    @Mock
    private AuthenticationManager authenticationManager; // @Mock: Spring Security'nin AuthenticationManager mock'u

    @InjectMocks
    private AuthServiceImpl authService; // @InjectMocks: Mock'ları AuthServiceImpl'e enjekte eder

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    /**
     * Her test öncesi ortak test verilerini hazırlar.
     * Bu method @BeforeEach ile işaretlendiği için her @Test method'undan önce çalışır.
     */
    @BeforeEach
    void setUp() {
        // Test için örnek RegisterRequest oluştur
        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        // Test için örnek LoginRequest oluştur
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        // Test için örnek User entity oluştur
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded-password")
                .roles("ROLE_USER")
                .status("ACTIVE")
                .build();
    }

    /**
     * Kullanıcı kaydı işlemini test eder.
     * Beklenen davranış: Yeni kullanıcı kaydedilir ve JWT token döner.
     */
    @Test
    void register_ShouldReturnToken_WhenValidRequest() {
        // Given: Mock davranışlarını ayarla
        when(userRepository.existsByEmail(anyString())).thenReturn(false); // Email mevcut değil
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password"); // Şifre encode edilsin
        when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token"); // JWT token üret
        when(userRepository.save(any(User.class))).thenReturn(user); // User kaydedilsin

        // When: register method'unu çağır
        AuthResponse response = authService.register(registerRequest);

        // Then: Sonuçları doğrula
        assertNotNull(response); // Response null olmamalı
        assertEquals("jwt-token", response.getToken()); // Token doğru olmalı
        assertEquals("Bearer", response.getTokenType()); // Token tipi Bearer olmalı

        // Verify: Mock method'larının çağrıldığını kontrol et
        verify(userRepository).existsByEmail("test@example.com"); // Email kontrolü yapıldı mı?
        verify(passwordEncoder).encode("password123"); // Şifre encode edildi mi?
        verify(userRepository).save(any(User.class)); // User kaydedildi mi?
        verify(jwtUtil).generateToken("test@example.com"); // Token üretildi mi?
    }

    /**
     * Mevcut email ile kayıt olmayı test eder.
     * Beklenen davranış: IllegalArgumentException fırlatılır.
     */
    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        // Given: Email zaten mevcut
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then: Exception bekle
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(registerRequest));

        assertEquals("Email already in use", exception.getMessage()); // Hata mesajı doğru mu?

        // Verify: existsByEmail çağrıldı, ama save çağrılmadı
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class)); // Kaydetme işlemi olmamalı
    }

    /**
     * Kullanıcı giriş işlemini test eder.
     * Beklenen davranış: Geçerli kimlik bilgileri ile JWT token döner.
     */
    @Test
    void authenticate_ShouldReturnToken_WhenValidCredentials() {
        // Given: Mock Authentication objesi
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication); // Authentication başarılı
        when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token"); // Token üret

        // When: authenticate method'unu çağır
        AuthResponse response = authService.authenticate(loginRequest);

        // Then: Sonuçları doğrula
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());

        // Verify: Mock method'ları çağrıldı mı?
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken("test@example.com");
    }
}