package com.subscription.payment.kafka;

import com.subscription.payment.dto.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentRequestConsumer {
    @KafkaListener(
        topics = "${app.kafka.topics.payment-requests}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "paymentRequestKafkaListenerContainerFactory"
    )
    public void consumePaymentRequest(PaymentRequest paymentRequest) {
        log.info("Received payment request event: subscriptionId={}, customerId={}, amount={}",
                paymentRequest.getSubscriptionId(), paymentRequest.getCustomerId(), paymentRequest.getAmount());
        // TODO: Payment işleme mantığı buraya gelecek
    }
}
