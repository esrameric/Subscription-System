# Payment Service

Bu servis ödeme işlemlerini yönetir ve Kafka ile subscription-service'e bildirim gönderir.

## Özellikler

- Ödeme kayıtlarını yönetir (PostgreSQL)
- Ödeme işlemlerini simüle eder
- Kafka üzerinden payment event'leri gönderir
- REST API ile ödeme oluşturma ve sorgulama

## Teknolojiler

- Spring Boot 3.2.12
- Java 21
- PostgreSQL
- Spring Kafka
- Lombok
- Spring Security

## API Endpoints

### Create Payment
```bash
POST /api/payments
Content-Type: application/json

{
  "subscriptionId": 1,
  "customerId": 1,
  "amount": 99.99,
  "currency": "TRY",
  "paymentMethod": "CREDIT_CARD",
  "description": "Monthly subscription payment"
}
```

### Get Payment by ID
```bash
GET /api/payments/{id}
```

### Get Payments by Subscription
```bash
GET /api/payments/subscription/{subscriptionId}
```

### Get Payments by Customer
```bash
GET /api/payments/customer/{customerId}
```

### Get All Payments
```bash
GET /api/payments
```

## Kafka Integration

### Producer Configuration
- Topic: `payment-events`
- Key: Subscription ID (String)
- Value: PaymentEvent (JSON)
- Serializer: JsonSerializer

### Payment Event Structure
```json
{
  "paymentId": 1,
  "subscriptionId": 1,
  "customerId": 1,
  "amount": 99.99,
  "currency": "TRY",
  "status": "SUCCESS",
  "paymentMethod": "CREDIT_CARD",
  "errorMessage": null,
  "eventTime": "2026-01-30T12:00:00Z"
}
```

## Payment Status

- **SUCCESS**: Ödeme başarılı
- **FAILED**: Ödeme başarısız
- **PENDING**: Ödeme beklemede
- **REFUNDED**: İade edildi

## Çalıştırma

### Docker ile PostgreSQL ve Kafka
```bash
docker-compose up -d
```

### Maven ile Çalıştırma
```bash
cd payment-service
mvn spring-boot:run
```

### H2 In-Memory Database ile Çalıştırma
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local-h2
```

## Configuration

### PostgreSQL (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/payment_db
    username: payment_user
    password: payment_pass
```

### Kafka (application.yml)
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

## Test

### Create Payment Test
```bash
curl -X POST http://localhost:8083/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "subscriptionId": 1,
    "customerId": 1,
    "amount": 99.99,
    "currency": "TRY",
    "paymentMethod": "CREDIT_CARD"
  }'
```

## Architecture

```
Payment Service
    ├── Controller Layer (REST API)
    ├── Service Layer (Business Logic)
    ├── Repository Layer (Database Access)
    ├── Kafka Producer (Event Publishing)
    └── Model & DTO
```

## Event Flow

1. Client ödeme isteği gönderir
2. Payment Service ödemeyi işler
3. Payment kaydı database'e yazılır
4. PaymentEvent Kafka'ya gönderilir
5. Subscription Service event'i dinler ve işler
