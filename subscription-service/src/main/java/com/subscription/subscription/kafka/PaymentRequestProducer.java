package com.subscription.subscription.kafka;

import com.subscription.subscription.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentRequestProducer {
    private static final Logger logger = LoggerFactory.getLogger(PaymentRequestProducer.class);
    private final KafkaTemplate<String, PaymentRequest> kafkaTemplate;

    public void sendPaymentRequest(PaymentRequest paymentRequest) {
        logger.info("Producing payment request event for subscriptionId={}, customerId={}", paymentRequest.getSubscriptionId(), paymentRequest.getCustomerId());
        kafkaTemplate.send("${app.kafka.topics.payment-requests}", paymentRequest);
    }
}
