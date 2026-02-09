#!/bin/bash

echo "🚀 Starting Subscription System..."
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Start Docker services
echo -e "${BLUE}📦 Starting Docker services (Kafka, PostgreSQL, Zookeeper)...${NC}"
docker-compose up -d

echo ""
echo -e "${YELLOW}⏳ Waiting for services to be ready (30 seconds)...${NC}"
sleep 30

echo ""
echo -e "${GREEN}✅ Infrastructure services started!${NC}"
echo ""
echo "Services:"
echo "  - PostgreSQL (Subscription): localhost:5432"
echo "  - PostgreSQL (Payment): localhost:5433"
echo "  - PostgreSQL (Notification): localhost:5434"
echo "  - Zookeeper: localhost:2181"
echo "  - Kafka Broker: localhost:9092"
echo "  - Kafka UI: http://localhost:8090"
echo ""
echo -e "${BLUE}📋 Service Startup Order:${NC}"
echo ""
echo "Now start the Spring Boot services in this order:"
echo ""
echo "1️⃣  Eureka Server (Port 8761):"
echo "   cd eureka-server && mvn spring-boot:run"
echo ""
echo "2️⃣  Customer Service (Port 8081):"
echo "   cd customer-service && mvn spring-boot:run"
echo ""
echo "3️⃣  Subscription Service (Port 8082):"
echo "   cd subscription-service && mvn spring-boot:run"
echo ""
echo "4️⃣  Payment Service (Port 8083) - Kafka Producer:"
echo "   cd payment-service && mvn spring-boot:run"
echo ""
echo "5️⃣  Notification Service (Port 8084) - Kafka Consumer:"
echo "   cd notification-service && mvn spring-boot:run"
echo ""
echo "6️⃣  API Gateway (Port 8080):"
echo "   cd api-gateway && mvn spring-boot:run"
echo ""
echo -e "${GREEN}🌐 After all services are running:${NC}"
echo "   - API Gateway: http://localhost:8080"
echo "   - Eureka Dashboard: http://localhost:8761"
echo "   - Kafka UI: http://localhost:8090"
echo ""
echo -e "${YELLOW}💡 Tip: Open 6 separate terminals to run all services${NC}"
