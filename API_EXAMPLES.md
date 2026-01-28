# API Testing Examples

This document provides curl examples for testing the API endpoints.

## Setup

First, make sure you have:
1. PostgreSQL running
2. `.env` file configured
3. Database migrated: `npm run prisma:migrate`
4. Server running: `npm run dev`

## Authentication

### Register a User

```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "Test User"
  }'
```

### Login

```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

Save the returned token for subsequent requests.

### Get Current User

```bash
curl -X GET http://localhost:3000/api/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Subscriptions

### Create a Subscription

```bash
curl -X POST http://localhost:3000/api/subscriptions \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "tier": "PREMIUM",
    "amount": 9.99,
    "currency": "USD",
    "billingCycle": "MONTHLY"
  }'
```

### Get All Subscriptions

```bash
curl -X GET http://localhost:3000/api/subscriptions \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Get Suspicious Subscriptions

```bash
curl -X GET http://localhost:3000/api/subscriptions/suspicious \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Flag a Subscription as Suspicious

```bash
curl -X PATCH http://localhost:3000/api/subscriptions/SUBSCRIPTION_ID/flag \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Unusual payment pattern detected"
  }'
```

### Approve a Subscription

```bash
curl -X PATCH http://localhost:3000/api/subscriptions/SUBSCRIPTION_ID/approve \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Cancel a Subscription

```bash
curl -X PATCH http://localhost:3000/api/subscriptions/SUBSCRIPTION_ID/cancel \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Transactions

### Get All Transactions

```bash
curl -X GET "http://localhost:3000/api/transactions?page=1&limit=10" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Create a Transaction

```bash
curl -X POST http://localhost:3000/api/transactions \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "SUBSCRIPTION_PAYMENT",
    "amount": 9.99,
    "currency": "USD",
    "description": "Monthly subscription payment"
  }'
```

## Reminders

### Get All Reminders

```bash
curl -X GET http://localhost:3000/api/reminders \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Create a Reminder

```bash
curl -X POST http://localhost:3000/api/reminders \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "SUBSCRIPTION_RENEWAL",
    "title": "Subscription Renewal",
    "message": "Your subscription will renew in 3 days",
    "scheduledAt": "2026-02-01T10:00:00Z"
  }'
```

### Mark Reminder as Read

```bash
curl -X PATCH http://localhost:3000/api/reminders/REMINDER_ID/read \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Delete a Reminder

```bash
curl -X DELETE http://localhost:3000/api/reminders/REMINDER_ID \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## User Conversions

### Upgrade to Premium

```bash
curl -X POST http://localhost:3000/api/conversions/upgrade \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 9.99,
    "currency": "USD",
    "billingCycle": "MONTHLY"
  }'
```

### Downgrade to Free

```bash
curl -X POST http://localhost:3000/api/conversions/downgrade \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Analytics

### Get Subscription Metrics

```bash
curl -X GET http://localhost:3000/api/analytics/subscriptions \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Get Revenue Metrics (Premium Only)

```bash
curl -X GET http://localhost:3000/api/analytics/revenue \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Get User Engagement Metrics

```bash
curl -X GET http://localhost:3000/api/analytics/engagement \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Get Conversion Metrics (Premium Only)

```bash
curl -X GET http://localhost:3000/api/analytics/conversions \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Health Check

```bash
curl -X GET http://localhost:3000/health
```
