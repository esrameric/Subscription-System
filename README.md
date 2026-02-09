# Subscription-System
A microservices-based subscription management system built with Spring Boot, using API Gateway, Eureka service discovery, JWT authentication, Kafka event streaming, and Docker.

## Architecture

This system consists of the following microservices:

### 1. API Gateway (Port 8080)
- Routes requests to appropriate microservices
- Handles authentication and authorization
- Centralized entry point for all client requests

### 2. Customer Service (Port 8081)
- Manages customer accounts and profiles
- User registration and authentication
- JWT token generation and validation

### 3. Subscription Service (Port 8082)
- Handles subscription plans and customer subscriptions
- Subscription lifecycle management (create, renew, cancel)
- **Kafka Consumer**: Listens to payment events to update subscription status
  - SUCCESS → Renews subscription (extends nextRenewalDate)
  - FAILED → Suspends subscription
- Integration with payment service

### 4. Payment Service (Port 8083)
- Processes payments for subscriptions
- **Kafka Producer**: Publishes payment events to Kafka
- Supports multiple payment methods
- Database: PostgreSQL (Port 5433)

### 5. Notification Service (Port 8084) ✨ NEW
- **Kafka Consumer**: Listens to payment and subscription events
- Sends email notifications for payment status
- Supports multiple notification channels (Email, SMS, Push)
- Database: PostgreSQL (Port 5434)
- Email templates for various notification types

### 6. Eureka Server (Port 8761)
- Service discovery and registration
- Health monitoring of microservices

## Technology Stack

- **Backend**: Spring Boot 3.2.12
- **Database**: PostgreSQL 16
- **Message Broker**: Apache Kafka (with Zookeeper)
- **Service Discovery**: Eureka
- **API Gateway**: Spring Cloud Gateway
- **Security**: JWT, Spring Security
- **Event Streaming**: Spring Kafka
- **Email**: Spring Mail (SMTP)
- **Containerization**: Docker & Docker Compose

## Kafka Integration

### Event Flow
```
Payment Service (Producer)
         │
         ▼
   payment-events topic
         │
    ┌────┴────┐
    ▼         ▼
Subscription   Notification
  Service       Service
(Status update) (Send email)
```

### Topics
- **payment-events**: Payment status updates (SUCCESS, FAILED, PENDING)
- **subscription-events**: Subscription lifecycle events

### Consumer Groups
- **subscription-service-group**: Updates subscription status based on payment result
- **notification-service-group**: Sends email/SMS notifications to customers

> **Note**: Different consumer groups receive the same message independently. This enables multiple services to react to the same event.

### Features
- Asynchronous event processing
- Guaranteed message delivery with manual acknowledgment
- Idempotent producers
- Retry mechanisms
- Event persistence in notification database

## Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL (if running services locally)

## Getting Started

### 1. Start Infrastructure Services (Kafka, PostgreSQL, Eureka)

```bash
docker-compose up -d
```

This will start:
- PostgreSQL instances for subscription, payment, and notification services
- Zookeeper (for Kafka)
- Kafka broker
- Kafka UI (available at http://localhost:8090)

### 2. Start Microservices

#### Option A: Using Maven (Development)

```bash
# Terminal 1 - Eureka Server
cd eureka-server
mvn spring-boot:run

# Terminal 2 - Customer Service
cd customer-service
mvn spring-boot:run

# Terminal 3 - Subscription Service
cd subscription-service
mvn spring-boot:run

# Terminal 4 - Payment Service
cd payment-service
mvn spring-boot:run

# Terminal 5 - Notification Service
cd notification-service
mvn spring-boot:run

# Terminal 6 - API Gateway
cd api-gateway
mvn spring-boot:run
```

#### Option B: Using JAR files

```bash
# Build all services
mvn clean package

# Run services
java -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar
java -jar customer-service/target/customer-service-0.0.1-SNAPSHOT.jar
java -jar subscription-service/target/subscription-service-0.0.1-SNAPSHOT.jar
java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar
java -jar notification-service/target/notification-service-0.0.1-SNAPSHOT.jar
java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
```

## Service URLs

- **API Gateway**: http://localhost:8080
- **Customer Service**: http://localhost:8081
- **Subscription Service**: http://localhost:8082
- **Payment Service**: http://localhost:8083
- **Notification Service**: http://localhost:8084
- **Eureka Dashboard**: http://localhost:8761
- **Kafka UI**: http://localhost:8090

## Database Connections

- **Subscription DB**: localhost:5432 (user: subscription_user, db: subscription_db)
- **Payment DB**: localhost:5433 (user: payment_user, db: payment_db)
- **Notification DB**: localhost:5434 (user: notification_user, db: notification_db)

## Testing the System

### 1. Register a Customer
```bash
curl -X POST http://localhost:8080/api/customers/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/customers/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "password": "password123"
  }'
```

### 3. Create a Subscription
```bash
curl -X POST http://localhost:8080/api/subscriptions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "customerId": 1,
    "planName": "Premium",
    "amount": 29.99
  }'
```

### 4. Process Payment
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "subscriptionId": 1,
    "amount": 29.99,
    "paymentMethod": "CREDIT_CARD"
  }'
```

### 5. Check Notifications
```bash
curl -X GET http://localhost:8080/api/notifications/customer/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Kafka Event Monitoring

Access Kafka UI at http://localhost:8090 to:
- View payment-events topic
- Monitor message flow
- Check consumer group status (notification-service-group)
- Debug message consumption

## Email Configuration

To enable email notifications, update `notification-service/src/main/resources/application.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
```

For Gmail, create an App Password from Google Account settings.

## Development Tips

### Using H2 Database (Local Development)

Each service can run with H2 in-memory database:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local-h2
```

### Viewing Kafka Messages

```bash
# List topics
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

# Consume payment-events
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic payment-events \
  --from-beginning
```

## Project Structure

```
subscription-system/
├── api-gateway/          # API Gateway service
├── customer-service/     # Customer management
├── subscription-service/ # Subscription management
├── payment-service/      # Payment processing + Kafka producer
├── notification-service/ # Notification handling + Kafka consumer
├── eureka-server/        # Service discovery
├── docker-compose.yml    # Infrastructure setup
└── README.md            # This file
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

