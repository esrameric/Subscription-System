package com.subscription.notification.service;

import com.subscription.notification.dto.PaymentEvent;
import com.subscription.notification.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Notification Template Service
 * 
 * Bildirim şablonlarını yönetir:
 * - Ödeme durumuna göre email konu başlıkları
 * - Ödeme durumuna göre email içerikleri
 * - Template içine dinamik veri enjeksiyonu
 * - Farklı dillerde template desteği (gelecekte)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTemplateService {

    /**
     * Ödeme durumuna göre bildirim konusu (subject) döner
     * 
     * @param status Ödeme durumu (SUCCESS, FAILED, PENDING)
     * @return Email konu başlığı
     */
    public String getPaymentNotificationSubject(String status) {
        return switch (status) {
            case "SUCCESS" -> "Payment Successful - Subscription Renewed";
            case "FAILED" -> "Payment Failed - Action Required";
            case "PENDING" -> "Payment Processing - Please Wait";
            default -> "Payment Notification";
        };
    }

    /**
     * Ödeme event bilgilerine göre email içeriği oluşturur
     * 
     * Template içerisine şu bilgiler eklenir:
     * - Ödeme tutarı (formatlanmış)
     * - Para birimi
     * - Ödeme yöntemi
     * - İşlem ID
     * - Tarih/saat
     * - Hata mesajı (varsa)
     * 
     * @param event Kafka'dan gelen payment event
     * @return Formatlanmış email içeriği
     */
    public String getPaymentNotificationContent(PaymentEvent event) {
        // Para birimi formatı (örn: $123.45)
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        String formattedAmount = currencyFormat.format(event.getAmount());

        // Ödeme durumuna göre uygun template'i seç ve event bilgilerini ekle
        return switch (event.getStatus()) {
            // Başarılı ödeme template'i
            case "SUCCESS" -> String.format("""
                Dear Customer,
                
                Your payment has been processed successfully!
                
                Payment Details:
                - Amount: %s %s
                - Payment Method: %s
                - Transaction ID: %s
                - Date: %s
                
                Your subscription has been renewed and is now active.
                
                Thank you for your business!
                
                Best regards,
                Subscription System Team
                """,
                    formattedAmount,
                    event.getCurrency(),
                    event.getPaymentMethod(),
                    event.getPaymentId(),
                    event.getEventTime()
            );

            // Başarısız ödeme template'i - müşteriyi bilgilendir ve aksiyona yönlendir
            case "FAILED" -> String.format("""
                Dear Customer,
                
                We were unable to process your payment.
                
                Payment Details:
                - Amount: %s %s
                - Payment Method: %s
                - Reason: %s
                - Date: %s
                
                Please update your payment information or try again.
                
                If you need assistance, please contact our support team.
                
                Best regards,
                Subscription System Team
                """,
                    formattedAmount,
                    event.getCurrency(),
                    event.getPaymentMethod(),
                    event.getErrorMessage() != null ? event.getErrorMessage() : "Payment declined",
                    event.getEventTime()
            );

            // Bekleyen ödeme template'i - müşteriyi bilgilendir
            case "PENDING" -> String.format("""
                Dear Customer,
                
                Your payment is being processed.
                
                Payment Details:
                - Amount: %s %s
                - Payment Method: %s
                - Transaction ID: %s
                - Date: %s
                
                You will receive a confirmation once the payment is completed.
                
                Best regards,
                Subscription System Team
                """,
                    formattedAmount,
                    event.getCurrency(),
                    event.getPaymentMethod(),
                    event.getPaymentId(),
                    event.getEventTime()
            );

            // Bilinmeyen durum - generic template
            default -> "Payment notification";
        };
    }

    /**
     * Ödeme durumunu NotificationType enum'una dönüştürür
     * 
     * Bu mapping bildirim geçmişinde ve filtrelemede kullanılır
     * 
     * @param status Ödeme durumu string (SUCCESS, FAILED, PENDING)
     * @return NotificationType enum değeri
     */
    public NotificationType getNotificationTypeFromPaymentStatus(String status) {
        return switch (status) {
            case "SUCCESS" -> NotificationType.PAYMENT_SUCCESS;
            case "FAILED" -> NotificationType.PAYMENT_FAILED;
            case "PENDING" -> NotificationType.PAYMENT_PENDING;
            default -> NotificationType.PAYMENT_PENDING;
        };
    }
}
