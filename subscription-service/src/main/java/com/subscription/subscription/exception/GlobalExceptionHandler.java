package com.subscription.subscription.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler - Merkezi Exception Yönetimi
 * 
 * Exception Handling Stratejisi:
 * - Tüm controller'larda fırlatılan exception'ları yakalar
 * - Tutarlı error response formatı sağlar
 * - Client-friendly error mesajları döndürür
 * - HTTP status code'ları standardize eder
 * 
 * @RestControllerAdvice:
 * - @ControllerAdvice + @ResponseBody birleşimi
 * - Global exception handler için kullanılır
 * - Tüm @RestController'ları dinler
 * - AOP (Aspect Oriented Programming) prensibiyle çalışır
 * 
 * @ExceptionHandler:
 * - Belirli bir exception tipini yakalar
 * - Method parametresi exception objesidir
 * - ResponseEntity döndürür (HTTP response)
 * 
 * Avantajları:
 * + Kod tekrarını önler (her controller'da try-catch gereksiz)
 * + Centralized error handling
 * + Tutarlı error response formatı
 * + Maintenance kolaylığı
 * 
 * Error Response Formatı:
 * {
 *   "timestamp": "2026-01-27T10:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Validation failed",
 *   "fieldErrors": {
 *     "email": "Email is required",
 *     "password": "Password must be at least 8 characters"
 *   }
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Validation Exception Handler
     * 
     * MethodArgumentNotValidException:
     * - @Valid annotation'lı request body validation başarısız olduğunda fırlatılır
     * - Spring Boot otomatik olarak fırlatır
     * - DTO'lardaki @NotNull, @NotBlank, @Size gibi validationlar
     * 
     * Kullanım Senaryosu:
     * POST /api/v1/offers
     * {
     *   "name": "",           // @NotBlank hatası
     *   "price": -10,         // @DecimalMin hatası
     *   "period": null        // @NotNull hatası
     * }
     * 
     * Response:
     * HTTP 400 Bad Request
     * {
     *   "timestamp": "2026-01-27T10:30:00",
     *   "status": 400,
     *   "error": "Validation Failed",
     *   "fieldErrors": {
     *     "name": "Offer adı boş olamaz",
     *     "price": "Fiyat 0.01'den büyük olmalıdır",
     *     "period": "Periyod uzunluğu belirtilmelidir"
     *   }
     * }
     * 
     * BindingResult:
     * - Validation hatalarını tutar
     * - getAllErrors(): Tüm hataları listeler
     * - FieldError: Alan bazlı hata detayları
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        // Error response map'i oluştur
        Map<String, Object> errors = new HashMap<>();
        errors.put("timestamp", LocalDateTime.now());  // Hata zamanı
        errors.put("status", HttpStatus.BAD_REQUEST.value());  // 400
        errors.put("error", "Validation Failed");  // Hata tipi
        
        // Field-level error'ları topla
        Map<String, String> fieldErrors = new HashMap<>();
        
        // BindingResult'tan tüm error'ları al
        ex.getBindingResult().getAllErrors().forEach(error -> {
            // FieldError: Hangi field'da hata var
            String fieldName = ((FieldError) error).getField();
            
            // Default message: @NotBlank(message = "...") içindeki mesaj
            String errorMessage = error.getDefaultMessage();
            
            // Field -> Error mesaj mapping'i
            fieldErrors.put(fieldName, errorMessage);
        });
        
        // Field error'ları response'a ekle
        errors.put("fieldErrors", fieldErrors);
        
        // HTTP 400 Bad Request döndür
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * IllegalArgumentException Handler
     * 
     * IllegalArgumentException:
     * - Business logic validation hatası için kullanılır
     * - Service layer'da fırlatılır
     * - Örnek senaryolar:
     *   * Offer not found
     *   * Subscription not found
     *   * Duplicate subscription
     *   * Invalid status transition
     * 
     * Kullanım Senaryosu:
     * Service'de:
     * if (!offerRepository.existsById(id)) {
     *     throw new IllegalArgumentException("Offer not found: " + id);
     * }
     * 
     * Response:
     * HTTP 400 Bad Request
     * {
     *   "timestamp": "2026-01-27T10:30:00",
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "Offer not found: 123"
     * }
     * 
     * Not:
     * - Daha gelişmiş projelerde custom exception'lar kullanılır
     * - Örnek: ResourceNotFoundException, DuplicateResourceException
     * - Her custom exception için ayrı handler yazılır
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        // Error response oluştur
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());  // 400
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());  // Exception mesajı
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Generic Exception Handler
     * 
     * Catch-All Handler:
     * - Yukarıdaki handler'ların yakalamadığı tüm exception'ları yakalar
     * - Unexpected error'lar için
     * - NullPointerException, RuntimeException, vb.
     * 
     * HTTP 500 Internal Server Error:
     * - Server-side hata
     * - Client'ın hatası değil, backend'in hatası
     * - Production'da detaylı error mesajı gösterilmemeli (güvenlik)
     * 
     * Security:
     * - Production'da ex.getMessage() gösterilmemeli
     * - Generic mesaj: "An unexpected error occurred"
     * - Detaylı log server-side'da tutulur (file, ELK, vb.)
     * 
     * Response:
     * HTTP 500 Internal Server Error
     * {
     *   "timestamp": "2026-01-27T10:30:00",
     *   "status": 500,
     *   "error": "Internal Server Error",
     *   "message": "An unexpected error occurred"
     * }
     * 
     * Logging:
     * - Bu handler'da exception log'lanmalı (logger.error())
     * - Stack trace kaydedilmeli
     * - Alerting sistemi tetiklenmeli (production)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        // Exception'ı logla
        System.err.println("=== UNEXPECTED ERROR ===");
        ex.printStackTrace();
        
        // Error response oluştur
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());  // 500
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred");  // Generic mesaj
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * İleride Eklenebilecek Handler'lar:
     * 
     * @ExceptionHandler(ResourceNotFoundException.class)
     * - Custom exception: Kaynak bulunamadı
     * - HTTP 404 Not Found
     * 
     * @ExceptionHandler(DuplicateResourceException.class)
     * - Custom exception: Duplicate kayıt
     * - HTTP 409 Conflict
     * 
     * @ExceptionHandler(UnauthorizedException.class)
     * - Custom exception: Yetkisiz erişim
     * - HTTP 401 Unauthorized
     * 
     * @ExceptionHandler(ForbiddenException.class)
     * - Custom exception: Erişim yasak
     * - HTTP 403 Forbidden
     * 
     * @ExceptionHandler(DataIntegrityViolationException.class)
     * - Database constraint violation
     * - HTTP 409 Conflict
     */
}
