#!/bin/bash

echo "🛑 Stopping Subscription System..."
echo ""

# Colors
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${RED}Stopping Docker services...${NC}"
docker-compose down

echo ""
echo "✅ All infrastructure services stopped!"
echo ""
echo "Note: Spring Boot services need to be stopped manually (Ctrl+C in their terminals)"
