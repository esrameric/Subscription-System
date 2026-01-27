package com.subscription.customer.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.customer.auth.dto.AuthResponse;
import com.subscription.customer.auth.dto.LoginRequest;
import com.subscription.customer.auth.dto.RegisterRequest;
import com.subscription.customer.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController için entegrasyon testleri.
 * @SpringBootTest ile full application context yükler.
 * MockMvc kullanarak HTTP endpoint'lerini test eder.
 * AuthService mock'lanır çünkü sadece controller katmanını test etmek istiyoruz.
 */
@SpringBootTest
class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext; // Spring application context

    private MockMvc mockMvc; // MockMvc: Spring MVC'nin mock versiyonu, HTTP isteklerini simüle eder

    @Autowired
    private ObjectMapper objectMapper; // ObjectMapper: JSON serileştirme/deserileştirme için Jackson kütüphanesi

    @MockBean
    private AuthService authService; // AuthService mock'u: Spring context'te gerçek bean yerine mock kullanılır

    /**
     * Test setup: MockMvc'yi WebApplicationContext ile başlatır.
     * @MockBean ile AuthService otomatik olarak mock'lanır.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        // @MockBean sayesinde AuthService zaten mock edilmiş durumda, ekstra işlem gerekmiyor
    }

    /**
     * Kullanıcı kaydı endpoint'ini test eder.
     * Beklenen davranış: Geçerli kayıt isteği ile 200 OK ve JWT token döner.
     */
    @Test
    void register_ShouldReturnToken_WhenValidRequest() throws Exception {
        // Given: Test verilerini hazırla
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponse response = new AuthResponse("jwt-token-here");

        // Mock: AuthService.register çağrıldığında response döndür
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // When & Then: POST /api/v1/auth/register isteği gönder ve sonucu doğrula
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // HTTP 200 bekle
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token-here")) // JSON response'da token alanı kontrol et
                .andExpect(jsonPath("$.tokenType").value("Bearer")); // tokenType alanı kontrol et
    }

    /**
     * Kullanıcı girişi endpoint'ini test eder.
     * Beklenen davranış: Geçerli giriş isteği ile 200 OK ve JWT token döner.
     */
    @Test
    void login_ShouldReturnToken_WhenValidCredentials() throws Exception {
        // Given: Test verilerini hazırla
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponse response = new AuthResponse("jwt-token-here");

        // Mock: AuthService.authenticate çağrıldığında response döndür
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(response);

        // When & Then: POST /api/v1/auth/login isteği gönder ve sonucu doğrula
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token-here"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    /**
     * Geçersiz kayıt isteği test eder (örneğin boş email).
     * Beklenen davranış: 400 Bad Request döner çünkü @Valid annotation validation hatası yakalar.
     */
    @Test
    void register_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given: Geçersiz request (boş email)
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail(""); // Geçersiz: boş string
        request.setPassword("password123");

        // When & Then: POST isteği gönder ve 400 Bad Request bekle
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Validation hatası için 400
    }
}