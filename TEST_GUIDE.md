# Subscription System - Test ve Kullanım Rehberi

## 🚀 Çalışan Servisler

### Altyapı Servisleri
- ✅ **Kafka** (localhost:9092) - Message broker
- ✅ **Zookeeper** (localhost:2181) - Kafka coordinator
- ✅ **Kafka UI** (http://localhost:8090) - Kafka monitoring
- ✅ **PostgreSQL** - 3 adet database (ports: 5432, 5433, 5434)
- ✅ **Eureka Server** (http://localhost:8761) - Service discovery

### Mikroservisler
- ✅ **Subscription Service** (port 8082) - Abonelik yönetimi
- ✅ **Payment Service** (port 8083) - Ödeme işlemleri + Kafka Producer
- ✅ **Notification Service** (port 8084) - Bildirimler + Kafka Consumer

## 🔄 Subscription Renewal İş Akışı

### Senaryo: Subscription Yenileme (Renewal)

1. **Manuel Yenileme İsteği**
   ```bash
   POST /api/v1/subscriptions/{id}/renew
   ```

2. **İşlem Adımları**:
   
   a) **Subscription Service** →
   - Subscription bilgilerini getirir
   - Offer'dan fiyat bilgisini alır
   - **Payment Service**'e Feign Client ile ödeme isteği gönderir
   
   b) **Payment Service** →
   - Ödeme işlemini simüle eder
   - Ödeme kaydını database'e yazar
   - **Kafka'ya payment-events topic'ine event publish eder**:
     ```json
     {
       "paymentId": 123,
       "subscriptionId": 456,
       "customerId": 789,
       "status": "SUCCESS", // veya "FAILED"
       "amount": 29.99,
       "timestamp": "2026-01-30T12:00:00Z"
     }
     ```
   
   c) **Subscription Service (Kafka Consumer)** →
   - payment-events topic'ini dinler
   - Ödeme başarılıysa: `renewSubscription()` çağırır
     - nextRenewalDate güncellenir (şimdi + 1 ay)
     - Status: ACTIVE
   - Ödeme başarısızsa: `suspendSubscription()` çağırır
     - Status: SUSPEND
   
   d) **Notification Service (Kafka Consumer)** →
   - payment-events topic'ini dinler
   - Email/SMS/Push notification gönderir
   - Notification database'e kaydeder

## 📊 Sistemi Test Etme

### 1. Eureka Dashboard
```bash
http://localhost:8761
```
- Tüm servislerin kayıtlı olduğunu görün
- SUBSCRIPTION-SERVICE ve PAYMENT-SERVICE görünmeli

### 2. Kafka UI
```bash
http://localhost:8090
```
- payment-events topic'ini görün
- Consumer groups: subscription-service-group, notification-service-group

### 3. Manuel Test (Postman veya curl)

#### a) Offer Oluşturma (Security disabled endpoint)
```bash
curl -X POST http://localhost:8082/api/v1/offers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Premium Monthly",
    "description": "Premium features",
    "price": 29.99,
    "period": 1
  }'
```

#### b) Subscription Oluşturma
Not: Security konfigürasyonuna göre JWT token gerekebilir

#### c) Subscription Renewal
```bash
curl -X POST http://localhost:8082/api/v1/subscriptions/1/renew \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. Log Monitoring
```bash
# Subscription Service
tail -f /tmp/subscription.log

# Payment Service  
tail -f /tmp/payment.log

# Notification Service
tail -f /tmp/notification.log

# Eureka Server
tail -f /tmp/eureka.log
```

## 🔍 Başarı Kontrolü

### Ödeme Başarılı Senaryosu
1. Payment Service → SUCCESS status döner
2. Kafka'ya event publish edilir
3. Subscription Service → nextRenewalDate günceller
4. Notification Service → Email/bildirim gönderir
5. Subscription status: ACTIVE kalır

### Ödeme Başarısız Senaryosu
1. Payment Service → FAILED status döner
2. Kafka'ya event publish edilir
3. Subscription Service → status=SUSPEND yapar
4. Notification Service → Hata bildirimi gönderir

## 🛠️ Troubleshooting

### Service çalışmıyor?
```bash
# Logları kontrol et
tail -100 /tmp/[service-name].log

# Port kullanımda mı?
lsof -i :8082
lsof -i :8083
```

### Kafka bağlantı hatası?
```bash
docker compose ps
# Kafka ve Zookeeper UP olmalı
```

### Eureka'ya kayıt olmadı?
```bash
# Service log'unda şunu ara:
grep "Registering application" /tmp/subscription.log
```

## 📝 Önemli Notlar

1. **Security**: Şu an temel JWT security var. Test için actuator endpoint'leri açık.

2. **Payment Simulation**: Payment Service gerçek ödeme gateway'i kullanmıyor, simülasyon yapıyor.

3. **Scheduled Renewal**: SubscriptionRenewalJob her gün 00:00'da otomatik çalışır ve yenilenmesi gereken abonelikleri process eder.

4. **Feign Client**: Subscription Service, Payment Service'e Eureka üzerinden bağlanır (service discovery).

5. **Kafka Event Flow**: 
   - Payment → Kafka → Subscription (renewal)
   - Payment → Kafka → Notification (email/sms)

## 🎯 Sonraki Adımlar

- [ ] Customer Service'i ekle ve JWT authentication'ı düzelt
- [ ] API Gateway ekle
- [ ] Docker Compose'a tüm servisleri ekle
- [ ] Integration test'ler yaz
- [ ] Circuit breaker ekle (Resilience4j)
- [ ] Distributed tracing (Sleuth + Zipkin)
- [ ] Config Server ekle
