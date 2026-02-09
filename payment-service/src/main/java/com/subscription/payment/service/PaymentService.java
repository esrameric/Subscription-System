package com.subscription.payment.service;

import com.subscription.payment.dto.PaymentEvent;
import com.subscription.payment.dto.PaymentRequest;
import com.subscription.payment.dto.PaymentResponse;
import com.subscription.payment.kafka.PaymentEventProducer;
import com.subscription.payment.model.Payment;
import com.subscription.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Payment Service
 * 
 * Ödeme işlemlerini yönetir
 * - Ödeme oluşturma
 * - Ödeme sorguları
 * - Kafka'ya event gönderme
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    /**
     * Yeni ödeme oluşturur
     * 
     * @param request Ödeme isteği
     * @return Oluşturulan ödeme
     */
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for subscriptionId={}, customerId={}, amount={}", 
                request.getSubscriptionId(), request.getCustomerId(), request.getAmount());

        // Ödeme işlemini simüle et
        String status = processPayment(request);
        String providerTransactionId = UUID.randomUUID().toString();
        String errorMessage = null;

        if ("FAILED".equals(status)) {
            errorMessage = "Ödeme gateway hatası - yetersiz bakiye";
        }

        // Payment entity oluştur
        Payment payment = Payment.builder()
                .subscriptionId(request.getSubscriptionId())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(status)
                .paymentMethod(request.getPaymentMethod())
                .providerTransactionId(providerTransactionId)
                .description(request.getDescription())
                .errorMessage(errorMessage)
                .build();

        // Veritabanına kaydet
        payment = paymentRepository.save(payment);
        log.info("Payment created: id={}, status={}", payment.getId(), payment.getStatus());

        // Kafka'ya event gönder
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(payment.getId())
                .subscriptionId(payment.getSubscriptionId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .errorMessage(payment.getErrorMessage())
                .eventTime(Instant.now())
                .build();

        paymentEventProducer.sendPaymentEvent(event);

        return mapToResponse(payment);
    }

    /**
     * Ödeme işlemini başlatır
     * Gerçek uygulamada burada payment gateway entegrasyonu olacak
     * Ödeme başlangıçta PENDING durumunda oluşturulur
     * Daha sonra confirmPayment veya failPayment ile sonuçlandırılır
     * 
     * @param request Ödeme isteği
     * @return Ödeme durumu (her zaman PENDING)
     */
    private String processPayment(PaymentRequest request) {
        // Ödeme başlangıçta PENDING durumunda oluşturulur
        // confirmPayment veya failPayment endpoint'leri ile sonuçlandırılır
        return "PENDING";
    }

    /**
     * Payment ID ile ödeme getirir
     */
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        return mapToResponse(payment);
    }

    /**
     * Subscription ID ile ödemeleri getirir
     */
    public List<PaymentResponse> getPaymentsBySubscriptionId(Long subscriptionId) {
        return paymentRepository.findBySubscriptionId(subscriptionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Customer ID ile ödemeleri getirir
     */
    public List<PaymentResponse> getPaymentsByCustomerId(Long customerId) {
        return paymentRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tüm ödemeleri getirir
     */
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Ödemeyi onaylar (SUCCESS durumuna geçirir)
     * 
     * @param paymentId Ödeme ID
     * @return Güncellenmiş ödeme
     */
    @Transactional
    public PaymentResponse confirmPayment(Long paymentId) {
        log.info("Confirming payment: paymentId={}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        
        if (!"PENDING".equals(payment.getStatus())) {
            throw new RuntimeException("Payment is not in PENDING status. Current status: " + payment.getStatus());
        }
        
        payment.setStatus("SUCCESS");
        payment = paymentRepository.save(payment);
        log.info("Payment confirmed: paymentId={}, status={}", payment.getId(), payment.getStatus());
        
        // Kafka'ya success event gönder
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(payment.getId())
                .subscriptionId(payment.getSubscriptionId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .errorMessage(null)
                .eventTime(Instant.now())
                .build();
        
        paymentEventProducer.sendPaymentEvent(event);
        
        return mapToResponse(payment);
    }

    /**
     * Ödemeyi başarısız olarak işaretler (FAILED durumuna geçirir)
     * 
     * @param paymentId Ödeme ID
     * @param errorMessage Hata mesajı
     * @return Güncellenmiş ödeme
     */
    @Transactional
    public PaymentResponse failPayment(Long paymentId, String errorMessage) {
        log.info("Failing payment: paymentId={}, reason={}", paymentId, errorMessage);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        
        if (!"PENDING".equals(payment.getStatus())) {
            throw new RuntimeException("Payment is not in PENDING status. Current status: " + payment.getStatus());
        }
        
        payment.setStatus("FAILED");
        payment.setErrorMessage(errorMessage != null ? errorMessage : "Payment failed");
        payment = paymentRepository.save(payment);
        log.info("Payment failed: paymentId={}, status={}", payment.getId(), payment.getStatus());
        
        // Kafka'ya failed event gönder
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(payment.getId())
                .subscriptionId(payment.getSubscriptionId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .errorMessage(payment.getErrorMessage())
                .eventTime(Instant.now())
                .build();
        
        paymentEventProducer.sendPaymentEvent(event);
        
        return mapToResponse(payment);
    }

    /**
     * Payment entity'yi response DTO'ya dönüştürür
     */
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .subscriptionId(payment.getSubscriptionId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .providerTransactionId(payment.getProviderTransactionId())
                .description(payment.getDescription())
                .errorMessage(payment.getErrorMessage())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
