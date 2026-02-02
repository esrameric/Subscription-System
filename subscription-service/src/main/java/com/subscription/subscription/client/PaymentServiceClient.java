package com.subscription.subscription.client;

import com.subscription.subscription.dto.PaymentRequest;
import com.subscription.subscription.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Payment Service Feign Client
 * 
 * OpenFeign kullanarak Payment Service'e HTTP istekleri yapar
 * - Declarative REST client (interface-based)
 * - Load balancing (Eureka integration ile)
 * - Circuit breaker pattern desteği
 * 
 * @FeignClient:
 * - name: Eureka'da kayıtlı servis adı
 * - url: Hard-coded URL (opsiyonel, Eureka kullanılmazsa)
 */
@FeignClient(name = "payment-service")
public interface PaymentServiceClient {

    /**
     * Payment oluşturma isteği gönderir
     * 
     * POST http://payment-service/api/payments
     * 
     * @param request PaymentRequest (subscriptionId, customerId, amount, paymentMethod)
     * @return PaymentResponse (paymentId, status, transactionId vb.)
     */
    @PostMapping("/api/payments")
    PaymentResponse createPayment(@RequestBody PaymentRequest request);
}
