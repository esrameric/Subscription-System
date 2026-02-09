package com.subscription.payment.repository;

import com.subscription.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Payment Repository
 * 
 * Ödeme kayıtları için veritabanı işlemleri
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Belirli bir subscription için tüm ödemeleri getirir
     */
    List<Payment> findBySubscriptionId(Long subscriptionId);

    /**
     * Belirli bir müşterinin tüm ödemelerini getirir
     */
    List<Payment> findByCustomerId(Long customerId);

    /**
     * Belirli bir müşteri ve subscription için ödemeleri getirir
     */
    List<Payment> findByCustomerIdAndSubscriptionId(Long customerId, Long subscriptionId);

    /**
     * Belirli bir durumdaki ödemeleri getirir
     */
    List<Payment> findByStatus(String status);

    /**
     * Provider transaction ID ile ödeme bulur
     */
    Optional<Payment> findByProviderTransactionId(String providerTransactionId);
}
