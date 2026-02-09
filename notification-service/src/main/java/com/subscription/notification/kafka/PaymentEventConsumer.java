package com.subscription.notification.kafka;

import com.subscription.notification.dto.PaymentEvent;
import com.subscription.notification.model.Notification;
import com.subscription.notification.model.NotificationChannel;
import com.subscription.notification.service.NotificationService;
import com.subscription.notification.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Payment Event Kafka Consumer
 * 
 * Kafka'dan gelen ödeme eventlerini dinler ve işler:
 * 1. payment-events topic'inden mesajları consume eder
 * 2. Ödeme durumuna göre bildirim oluşturur
 * 3. Email/SMS/Push bildirimi gönderir
 * 4. Manuel olarak mesajı acknowledge eder
 * 
 * Manual Acknowledgment kullanılır - mesaj başarıyla işlenmedikçe commit edilmez
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final NotificationService notificationService;
    private final NotificationTemplateService templateService;

    /**
     * Kafka'dan Payment Event'lerini dinler ve işler
     * 
     * @KafkaListener: Belirtilen topic'i dinler
     * - topics: payment-events topic'i
     * - groupId: notification-service-group (aynı gruptaki consumerlar yükü paylaşır)
     * - containerFactory: Manuel acknowledgment yapılandırması
     * 
     * @param event Gelen ödeme olayı (paymentId, customerId, status, amount vb.)
     * @param topic Mesajın geldiği topic adı
     * @param partition Mesajın geldiği partition numarası
     * @param offset Mesajın offset değeri (sıra numarası)
     * @param acknowledgment Manuel olarak mesajı onaylamak için kullanılır
     */
    @KafkaListener(
        topics = "${app.kafka.topics.payment-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentEvent(
            @Payload PaymentEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            // Gelen mesaj bilgilerini logla
            log.info("Received payment event from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
            log.info("Payment Event: paymentId={}, customerId={}, status={}, amount={}", 
                    event.getPaymentId(), event.getCustomerId(), event.getStatus(), event.getAmount());

            // Bildirim oluştur ve gönder
            processPaymentNotification(event);

            // Mesajı manuel olarak onayla (commit)
            // Bu sayede mesaj başarıyla işlendiği Kafka'ya bildirilir
            acknowledgment.acknowledge();
            log.info("Payment event processed and acknowledged successfully");

        } catch (Exception e) {
            // Hata durumunda mesajı ONAYLAMA
            // Mesaj tekrar işlenmek üzere kuyrukta kalacak
            log.error("Error processing payment event: {}", event.getPaymentId(), e);
            throw new RuntimeException("Failed to process payment event", e);
        }
    }

    /**
     * Payment event'inden bildirim oluşturur ve gönderir
     * 
     * İşlem Adımları:
     * 1. Ödeme durumuna göre konu (subject) oluştur
     * 2. Ödeme bilgilerini içeren içerik (content) hazırla
     * 3. Bildirim entity'si oluştur
     * 4. Email/SMS/Push gönder
     * 
     * @param event Kafka'dan gelen ödeme olayı
     */
    private void processPaymentNotification(PaymentEvent event) {
        // Bildirim konu ve içeriğini hazırla
        String subject = templateService.getPaymentNotificationSubject(event.getStatus());
        String content = templateService.getPaymentNotificationContent(event);

        // Notification entity'sini oluştur ve veritabanına kaydet
        Notification notification = notificationService.createNotification(
                event.getCustomerId(),
                templateService.getNotificationTypeFromPaymentStatus(event.getStatus()),
                NotificationChannel.EMAIL,
                getCustomerEmail(event.getCustomerId()), // Üretimde customer service'den çekilecek
                subject,
                content,
                event.getPaymentId()
        );

        // Bildirimi gönder (Email/SMS/Push)
        notificationService.sendNotification(notification);
    }

    /**
     * Müşteri email adresini getirir
     * 
     * TODO: Üretim ortamında Customer Service'den REST API ile çekilecek
     * Şu an için demo amaçlı placeholder döner
     * 
     * @param customerId Müşteri ID
     * @return Email adresi
     */
    private String getCustomerEmail(Long customerId) {
        return String.format("customer%d@example.com", customerId);
    }
}
