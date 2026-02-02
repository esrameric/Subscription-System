package com.subscription.subscription.kafka;

import com.subscription.subscription.dto.PaymentEvent;
import com.subscription.subscription.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Payment Event Consumer
 * 
 * Payment service'ten gelen ödeme olaylarını dinler
 * Başarılı ödemeler için subscription'ı yeniler
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final SubscriptionService subscriptionService;

    /**
     * Payment event'lerini dinler
     * 
     * @param event Gelen ödeme olayı
     */
    @KafkaListener(
        topics = "${app.kafka.topics.payment-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: paymentId={}, subscriptionId={}, status={}", 
                event.getPaymentId(), event.getSubscriptionId(), event.getStatus());

        try {
            // Ödeme başarılı ise subscription'ı işle
            if ("SUCCESS".equals(event.getStatus())) {
                handleSuccessfulPayment(event);
            } else if ("FAILED".equals(event.getStatus())) {
                handleFailedPayment(event);
            } else {
                log.info("Payment status is {}, no action needed for subscriptionId={}", 
                        event.getStatus(), event.getSubscriptionId());
            }
        } catch (Exception e) {
            log.error("Error processing payment event: paymentId={}, error={}", 
                    event.getPaymentId(), e.getMessage(), e);
            // TODO: Dead letter queue'ya gönder veya retry mekanizması ekle
        }
    }

    /**
     * Başarılı ödeme sonrası subscription güncelleme
     */
    private void handleSuccessfulPayment(PaymentEvent event) {
        log.info("Processing successful payment for subscriptionId={}", event.getSubscriptionId());
        
        try {
            // Subscription'ı yenile - nextRenewalDate'i güncelle
            subscriptionService.renewSubscription(event.getSubscriptionId());
            log.info("Subscription renewed successfully: subscriptionId={}", event.getSubscriptionId());
        } catch (Exception e) {
            log.error("Failed to renew subscription: subscriptionId={}, error={}", 
                    event.getSubscriptionId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Başarısız ödeme sonrası işlem
     */
    private void handleFailedPayment(PaymentEvent event) {
        log.warn("Payment failed for subscriptionId={}, errorMessage={}", 
                event.getSubscriptionId(), event.getErrorMessage());
        
        try {
            // Subscription'ı SUSPEND durumuna al
            subscriptionService.suspendSubscription(event.getSubscriptionId());
            log.info("Subscription suspended due to payment failure: subscriptionId={}", 
                    event.getSubscriptionId());
        } catch (Exception e) {
            log.error("Failed to suspend subscription: subscriptionId={}, error={}", 
                    event.getSubscriptionId(), e.getMessage(), e);
            throw e;
        }
    }
}
