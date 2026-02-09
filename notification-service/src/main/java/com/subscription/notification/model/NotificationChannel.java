package com.subscription.notification.model;

/**
 * Bildirim Gönderim Kanalları
 * 
 * Bildirimin hangi kanal üzerinden gönderileceğini belirtir:
 * 
 * - EMAIL: Email yoluyla gönderim (SMTP - Gmail, SendGrid vb.)
 * - SMS: SMS yoluyla gönderim (Twilio, AWS SNS, Vonage vb.)
 * - PUSH: Push notification (Firebase FCM, Apple APNS vb.)
 * 
 * Her müşteri tercihine göre farklı kanalları kullanabilir
 */
public enum NotificationChannel {
    EMAIL,   // Email bildirimi
    SMS,     // SMS bildirimi
    PUSH     // Push notification
}
