package com.subscription.customer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler sınıfı.
 * Uygulama genelindeki exception'ları yakalar ve standart JSON response döner.
 * @RestControllerAdvice: Tüm controller'lar için exception handling sağlar.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Validation hatalarını yakalar (@Valid annotation'dan gelen hatalar).
     * Örneğin: @NotBlank, @Email vb. validation constraint ihlalleri.
     * @param ex MethodArgumentNotValidException: Spring'in validation exception'ı
     * @return Map<String, String> olarak field hataları ve mesajları
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("timestamp", LocalDateTime.now()); // Hata zamanı
        errors.put("status", HttpStatus.BAD_REQUEST.value()); // HTTP status kodu
        errors.put("error", "Validation Failed"); // Genel hata açıklaması
        errors.put("message", "Input validation failed"); // Detay mesaj

        Map<String, String> fieldErrors = new HashMap<>();
        // Her validation hatası için field adı ve mesajı ekle
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField(); // Hatalı field adı
            String errorMessage = error.getDefaultMessage(); // Validation mesajı
            fieldErrors.put(fieldName, errorMessage);
        });
        errors.put("fieldErrors", fieldErrors); // Field-specific hatalar

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Genel IllegalArgumentException'ları yakalar.
     * Örneğin: Email zaten kullanımda hatası.
     * @param ex IllegalArgumentException: Business logic exception'ları
     * @return Standart hata response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage()); // Exception mesajını kullan

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Authentication hatalarını yakalar (yanlış şifre vb.).
     * Spring Security'nin BadCredentialsException'ı.
     * @param ex BadCredentialsException: Yanlış kimlik bilgileri
     * @return 401 Unauthorized response
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.UNAUTHORIZED.value());
        error.put("error", "Unauthorized");
        error.put("message", "Invalid username or password"); // Güvenlik için genel mesaj

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Genel exception'ları yakalar (beklenmeyen hatalar).
     * @param ex Exception: Tüm diğer exception'lar
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred"); // Üretimde detay verme

        // Loglama için (gerçek uygulamada logger kullan)
        System.err.println("Unexpected error: " + ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}