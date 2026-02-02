package com.subscription.subscription.subscription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * CreateSubscriptionRequest DTO
 * 
 * Yeni abonelik oluşturma request'i
 * 
 * Önemli Özellik:
 * - Customer ID YOKTUR!
 * - Customer ID, JWT token'dan alınır (authentication)
 * - Bu sayede kullanıcı sadece kendi adına abonelik oluşturabilir (güvenlik)
 * 
 * Security Pattern:
 * 1. Client login olur, JWT token alır
 * 2. Token'da customer ID/email bilgisi vardır
 * 3. API isteğinde token gönderilir (Authorization: Bearer <token>)
 * 4. Backend token'ı parse eder, customer ID'yi çıkarır
 * 5. Customer ID otomatik olarak request'e eklenir
 * 
 * Bu yaklaşım:
 * + Güvenli: Kullanıcı başkası adına abonelik oluşturamaz
 * + Kullanıcı dostu: Client'ın customer ID göndermesine gerek yok
 * - Token management gerektirir
 * 
 * JWT Token Örneği:
 * {
 *   "sub": "user@example.com",
 *   "customerId": 123,
 *   "exp": 1643123456
 * }
 */
@Getter
@Setter
public class CreateSubscriptionRequest {

    /**
     * Offer ID
     * 
     * Hangi paketi satın almak istediğini belirtir
     * 
     * @NotNull: Zorunlu alan
     * 
     * Örnek Request:
     * POST /api/v1/subscriptions
     * Authorization: Bearer eyJhbGc...
     * {
     *   "offerId": 1
     * }
     * 
     * Backend'de:
     * - Token'dan customer ID extract edilir (örn: 123)
     * - offerId ile offer database'den getirilir
     * - Yeni subscription oluşturulur
     */
    @NotNull(message = "Offer ID belirtilmelidir")
    private Long offerId;
}
