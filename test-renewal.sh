#!/bin/bash

# Test Script - Subscription Renewal with Payment

echo "========================================="
echo "Subscription Renewal Test"
echo "========================================="
echo ""

# 1. Önce bir Offer oluştur
echo "1. Creating an offer..."
OFFER_RESPONSE=$(curl -s -X POST http://localhost:8082/api/v1/offers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Premium Monthly",
    "description": "Premium features for 1 month",
    "price": 29.99,
    "period": 1,
    "status": "ACTIVE"
  }')

OFFER_ID=$(echo $OFFER_RESPONSE | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
echo "✅ Offer created with ID: $OFFER_ID"
echo "Response: $OFFER_RESPONSE"
echo ""

# 2. Subscription oluştur (customer ID = 123 olarak varsayalım)
echo "2. Creating a subscription..."
SUBSCRIPTION_RESPONSE=$(curl -s -X POST http://localhost:8082/api/v1/subscriptions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d "{
    \"offerId\": $OFFER_ID
  }")

SUBSCRIPTION_ID=$(echo $SUBSCRIPTION_RESPONSE | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
echo "✅ Subscription created with ID: $SUBSCRIPTION_ID"
echo "Response: $SUBSCRIPTION_RESPONSE"
echo ""

# 3. Subscription'ı görüntüle
echo "3. Getting subscription details..."
curl -s http://localhost:8082/api/v1/subscriptions/$SUBSCRIPTION_ID | jq '.'
echo ""

# 4. Subscription renewal (payment ile)
echo "4. Renewing subscription (with payment)..."
RENEWAL_RESPONSE=$(curl -s -X POST http://localhost:8082/api/v1/subscriptions/$SUBSCRIPTION_ID/renew \
  -H "Authorization: Bearer test-token")

echo "✅ Renewal Response:"
echo $RENEWAL_RESPONSE | jq '.'
echo ""

# 5. Kafka'dan notification event'lerini kontrol et
echo "5. Checking Kafka UI for payment events..."
echo "Visit: http://localhost:8090"
echo ""

# 6. Payment Service'den ödeme detaylarını çek
echo "6. Getting payment details..."
sleep 2
curl -s "http://localhost:8083/api/payments/subscription/$SUBSCRIPTION_ID" | jq '.'
echo ""

# 7. Subscription'ın güncellenmiş durumunu kontrol et
echo "7. Checking updated subscription (should be renewed if payment was successful)..."
sleep 3
curl -s http://localhost:8082/api/v1/subscriptions/$SUBSCRIPTION_ID | jq '.'
echo ""

echo "========================================="
echo "Test completed!"
echo "========================================="
echo ""
echo "Summary:"
echo "- Offer ID: $OFFER_ID"
echo "- Subscription ID: $SUBSCRIPTION_ID"
echo "- Check Kafka UI: http://localhost:8090"
echo "- Check Eureka: http://localhost:8761"
echo ""
