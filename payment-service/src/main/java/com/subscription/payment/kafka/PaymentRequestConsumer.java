package com.subscription.payment.kafka;

import com.subscription.payment.dto.PaymentRequest;
import com.subscription.payment.dto.PaymentResponse;
import com.subscription.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentRequestConsumer {
    private final PaymentService paymentService;
    @KafkaListener(
        topics = "${app.kafka.topics.payment-requests}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "paymentRequestKafkaListenerContainerFactory"
    )
    public void consumePaymentRequest(PaymentRequest paymentRequest) {
        log.info("Received payment request event: subscriptionId={}, customerId={}, amount={}",
                paymentRequest.getSubscriptionId(), paymentRequest.getCustomerId(), paymentRequest.getAmount());
        try {
            PaymentResponse response = paymentService.createPayment(paymentRequest);
            log.info("Payment processed from Kafka: paymentId={}, subscriptionId={}, status={}",
                    response.getPaymentId(), response.getSubscriptionId(), response.getStatus());
        } catch (Exception e) {
            log.error("Error processing payment request for subscriptionId={}: {}", paymentRequest.getSubscriptionId(),
                    e.getMessage(), e);
            throw e;
        }
    }
}
