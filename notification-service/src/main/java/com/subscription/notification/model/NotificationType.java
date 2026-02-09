package com.subscription.notification.model;

/**
 * Bildirim Tipleri
 * 
 * Sistemde gönderilebilecek farklı bildirim türlerini tanımlar:
 * 
 * Ödeme İle İlgili:
 * - PAYMENT_SUCCESS: Ödeme başarıyla tamamlandı
 * - PAYMENT_FAILED: Ödeme başarısız oldu
 * - PAYMENT_PENDING: Ödeme işleniyor
 * 
 * Abonelik İle İlgili:
 * - SUBSCRIPTION_CREATED: Yeni abonelik oluşturuldu
 * - SUBSCRIPTION_RENEWED: Abonelik yenilendi
 * - SUBSCRIPTION_CANCELLED: Abonelik iptal edildi
 * - SUBSCRIPTION_EXPIRED: Abonelik süresi doldu
 * - SUBSCRIPTION_EXPIRING_SOON: Abonelik yakında dolacak (hatırlatma)
 * 
 * Hesap İle İlgili:
 * - WELCOME: Hoş geldiniz mesajı (yeni kayıt)
 * - PASSWORD_RESET: Şifre sıfırlama
 * - ACCOUNT_VERIFIED: Hesap doğrulandı
 */
public enum NotificationType {
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    PAYMENT_PENDING,
    SUBSCRIPTION_CREATED,
    SUBSCRIPTION_RENEWED,
    SUBSCRIPTION_CANCELLED,
    SUBSCRIPTION_EXPIRED,
    SUBSCRIPTION_EXPIRING_SOON,
    WELCOME,
    PASSWORD_RESET,
    ACCOUNT_VERIFIED
}
