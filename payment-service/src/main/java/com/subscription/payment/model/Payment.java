package com.subscription.payment.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment Entity - Ödeme Kayıtları
 * 
 * Müşteri ödemelerini takip eder
 * - Her ödeme bir subscription'a aittir
 * - Ödeme durumu (SUCCESS, FAILED, PENDING)
 * - Ödeme yöntemi bilgisi
 */
@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Subscription ID
     * Hangi abonelik için ödeme yapıldı
     */
    @Column(nullable = false)
    private Long subscriptionId;

    /**
     * Customer ID
     * Hangi müşteri ödeme yaptı
     */
    @Column(nullable = false)
    private Long customerId;

    /**
     * Ödeme Tutarı
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * Para Birimi
     * Örnek: TRY, USD, EUR
     */
    @Column(nullable = false, length = 3)
    private String currency = "TRY";

    /**
     * Ödeme Durumu
     * SUCCESS: Ödeme başarılı
     * FAILED: Ödeme başarısız
     * PENDING: Ödeme beklemede
     * REFUNDED: İade edildi
     */
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * Ödeme Yöntemi
     * CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, WALLET
     */
    @Column(nullable = false, length = 50)
    private String paymentMethod;

    /**
     * Ödeme Sağlayıcı ID
     * Ödeme gateway'den dönen transaction ID
     */
    @Column(length = 100)
    private String providerTransactionId;

    /**
     * Ödeme Açıklaması
     */
    @Column(length = 500)
    private String description;

    /**
     * Hata Mesajı
     * Ödeme başarısız olduğunda nedeni
     */
    @Column(length = 1000)
    private String errorMessage;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
