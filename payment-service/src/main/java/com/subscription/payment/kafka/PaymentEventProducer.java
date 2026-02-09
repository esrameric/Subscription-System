package com.subscription.payment.kafka;

import com.subscription.payment.dto.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Payment Event Producer
 * 
 * Ödeme olaylarını Kafka'ya gönderir
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Value("${app.kafka.topics.payment-events}")
    private String paymentEventsTopic;

    /**
     * Ödeme olayını Kafka'ya gönderir
     * 
     * @param paymentEvent Gönderilecek olay
     */
    public void sendPaymentEvent(PaymentEvent paymentEvent) {
        log.info("Sending payment event to Kafka: {}", paymentEvent);
        
        try {
            // Kafka'ya asenkron olarak gönder
            CompletableFuture<SendResult<String, PaymentEvent>> future = 
                kafkaTemplate.send(paymentEventsTopic, 
                                 paymentEvent.getSubscriptionId().toString(), 
                                 paymentEvent);
            
            // Callback ile sonucu dinle
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Payment event sent successfully: paymentId={}, topic={}, partition={}, offset={}", 
                            paymentEvent.getPaymentId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send payment event: paymentId={}, error={}", 
                            paymentEvent.getPaymentId(), ex.getMessage(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error while sending payment event: paymentId={}, error={}", 
                    paymentEvent.getPaymentId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send payment event to Kafka", e);
        }
    }
}
