package com.subscription.payment.controller;

import com.subscription.payment.dto.PaymentRequest;
import com.subscription.payment.dto.PaymentResponse;
import com.subscription.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Payment Controller
 * 
 * Ödeme işlemleri için REST API endpoint'leri
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Yeni ödeme oluşturur
     * 
     * POST /api/payments
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Received payment creation request: subscriptionId={}", request.getSubscriptionId());
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Payment ID ile ödeme getirir
     * 
     * GET /api/payments/{paymentId}
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long paymentId) {
        log.info("Fetching payment: paymentId={}", paymentId);
        PaymentResponse response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Subscription ID ile ödemeleri getirir
     * 
     * GET /api/payments/subscription/{subscriptionId}
     */
    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsBySubscriptionId(@PathVariable Long subscriptionId) {
        log.info("Fetching payments for subscription: subscriptionId={}", subscriptionId);
        List<PaymentResponse> payments = paymentService.getPaymentsBySubscriptionId(subscriptionId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Customer ID ile ödemeleri getirir
     * 
     * GET /api/payments/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByCustomerId(@PathVariable Long customerId) {
        log.info("Fetching payments for customer: customerId={}", customerId);
        List<PaymentResponse> payments = paymentService.getPaymentsByCustomerId(customerId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Tüm ödemeleri getirir
     * 
     * GET /api/payments
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        log.info("Fetching all payments");
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    /**
     * Ödemeyi onaylar (SUCCESS durumuna geçirir)
     * 
     * POST /api/payments/{paymentId}/success
     */
    @PostMapping("/{paymentId}/success")
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable Long paymentId) {
        log.info("Confirming payment: paymentId={}", paymentId);
        PaymentResponse response = paymentService.confirmPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Ödemeyi başarısız olarak işaretler (FAILED durumuna geçirir)
     * 
     * POST /api/payments/{paymentId}/fail
     */
    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<PaymentResponse> failPayment(
            @PathVariable Long paymentId,
            @RequestParam(required = false) String errorMessage) {
        log.info("Failing payment: paymentId={}, reason={}", paymentId, errorMessage);
        PaymentResponse response = paymentService.failPayment(paymentId, errorMessage);
        return ResponseEntity.ok(response);
    }
}
