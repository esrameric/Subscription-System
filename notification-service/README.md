# Notification Service

Notification service that listens to Kafka events and sends notifications via email, SMS, or push notifications.

## Features

- **Kafka Consumer**: Listens to payment and subscription events
- **Email Notifications**: Sends email notifications using Spring Mail
- **Database Persistence**: Stores all notifications in PostgreSQL
- **Retry Mechanism**: Automatic retry for failed notifications
- **Multiple Channels**: Support for EMAIL, SMS, and PUSH notifications

## Technology Stack

- Spring Boot 3.2.12
- Spring Kafka
- Spring Data JPA
- PostgreSQL
- Spring Mail
- Lombok

## Configuration

### Application Properties

Key configurations in `application.yml`:

- **Server Port**: 8084
- **Database**: PostgreSQL on port 5434
- **Kafka**: Consumer group for payment and subscription events
- **Email**: SMTP configuration for sending emails

### Kafka Topics

The service listens to:
- `payment-events`: Payment status updates
- `subscription-events`: Subscription lifecycle events

## API Endpoints

### Get Customer Notifications
```
GET /api/notifications/customer/{customerId}
```

### Get Notification by ID
```
GET /api/notifications/{id}
```

### Get All Notifications
```
GET /api/notifications
```

## Running the Service

### Prerequisites

1. Kafka and Zookeeper running
2. PostgreSQL database running on port 5434
3. Email server configured (SMTP)

### Using Maven

```bash
cd notification-service
mvn spring-boot:run
```

### Using Docker Compose

```bash
docker-compose up -d
```

## Notification Types

- `PAYMENT_SUCCESS`: Payment processed successfully
- `PAYMENT_FAILED`: Payment processing failed
- `PAYMENT_PENDING`: Payment is being processed
- `SUBSCRIPTION_CREATED`: New subscription created
- `SUBSCRIPTION_RENEWED`: Subscription renewed
- `SUBSCRIPTION_CANCELLED`: Subscription cancelled
- `SUBSCRIPTION_EXPIRED`: Subscription expired
- `SUBSCRIPTION_EXPIRING_SOON`: Subscription about to expire

## Development

### Testing with H2 Database

To use H2 in-memory database for development:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local-h2
```

## Email Configuration

Update the following properties in `application.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
```

For Gmail, you need to generate an App Password from your Google Account settings.
