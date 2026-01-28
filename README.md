# sd-backend

Backend API for Subscription Management and Product Analysis Application

## Overview

This is a TypeScript-based backend application built with Express.js and Prisma ORM that provides comprehensive APIs for subscription management, product analysis, and data analytics. The application supports both Free and Premium user tiers with various features including:

- 🔐 User authentication and authorization
- 💳 Subscription management (Free/Premium)
- 🚨 Suspicious subscription detection and approval
- 📝 Transaction list management
- ⏰ Reminder system
- 🔄 User conversion flows (Free to Premium)
- 📊 Data analytics and metrics

## Tech Stack

- **Runtime:** Node.js
- **Language:** TypeScript
- **Framework:** Express.js
- **Database:** PostgreSQL
- **ORM:** Prisma
- **Authentication:** JWT
- **Validation:** express-validator

## Prerequisites

- Node.js (v16 or higher)
- PostgreSQL (v12 or higher)
- npm or yarn

## Installation

1. Clone the repository:
```bash
git clone https://github.com/gokhanaytekinn/sd-backend.git
cd sd-backend
```

2. Install dependencies:
```bash
npm install
```

3. Set up environment variables:
```bash
cp .env.example .env
```

Edit `.env` and configure your database connection and JWT secret:
```env
PORT=3000
NODE_ENV=development
DATABASE_URL="postgresql://user:password@localhost:5432/sd_backend?schema=public"
JWT_SECRET=your-secret-key-here
JWT_EXPIRES_IN=7d
CORS_ORIGIN=http://localhost:3000
```

4. Generate Prisma client:
```bash
npm run prisma:generate
```

5. Run database migrations:
```bash
npm run prisma:migrate
```

## Running the Application

### Development Mode
```bash
npm run dev
```

### Production Mode
```bash
npm run build
npm start
```

## API Documentation

### Base URL
```
http://localhost:3000/api
```

### Authentication

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

#### Get Current User
```http
GET /api/auth/me
Authorization: Bearer {token}
```

### Subscriptions

#### Get All Subscriptions
```http
GET /api/subscriptions
Authorization: Bearer {token}
Query Parameters: ?status=ACTIVE&isSuspicious=false
```

#### Get Subscription by ID
```http
GET /api/subscriptions/:id
Authorization: Bearer {token}
```

#### Create Subscription
```http
POST /api/subscriptions
Authorization: Bearer {token}
Content-Type: application/json

{
  "tier": "PREMIUM",
  "amount": 9.99,
  "currency": "USD",
  "billingCycle": "MONTHLY"
}
```

#### Cancel Subscription
```http
PATCH /api/subscriptions/:id/cancel
Authorization: Bearer {token}
```

#### Get Suspicious Subscriptions
```http
GET /api/subscriptions/suspicious
Authorization: Bearer {token}
```

#### Flag Subscription as Suspicious
```http
PATCH /api/subscriptions/:id/flag
Authorization: Bearer {token}
Content-Type: application/json

{
  "reason": "Unusual payment pattern detected"
}
```

#### Approve Subscription
```http
PATCH /api/subscriptions/:id/approve
Authorization: Bearer {token}
```

### Transactions

#### Get All Transactions
```http
GET /api/transactions
Authorization: Bearer {token}
Query Parameters: ?type=SUBSCRIPTION_PAYMENT&status=COMPLETED&page=1&limit=10
```

#### Get Transaction by ID
```http
GET /api/transactions/:id
Authorization: Bearer {token}
```

#### Create Transaction
```http
POST /api/transactions
Authorization: Bearer {token}
Content-Type: application/json

{
  "subscriptionId": "uuid",
  "type": "SUBSCRIPTION_PAYMENT",
  "amount": 9.99,
  "currency": "USD",
  "description": "Monthly subscription payment"
}
```

### Reminders

#### Get All Reminders
```http
GET /api/reminders
Authorization: Bearer {token}
Query Parameters: ?type=SUBSCRIPTION_RENEWAL&isRead=false
```

#### Create Reminder
```http
POST /api/reminders
Authorization: Bearer {token}
Content-Type: application/json

{
  "type": "SUBSCRIPTION_RENEWAL",
  "title": "Subscription Renewal",
  "message": "Your subscription will renew in 3 days",
  "scheduledAt": "2026-02-01T10:00:00Z"
}
```

#### Update Reminder
```http
PATCH /api/reminders/:id
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Updated Title"
}
```

#### Mark Reminder as Read
```http
PATCH /api/reminders/:id/read
Authorization: Bearer {token}
```

#### Delete Reminder
```http
DELETE /api/reminders/:id
Authorization: Bearer {token}
```

### User Conversions

#### Upgrade to Premium
```http
POST /api/conversions/upgrade
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 9.99,
  "currency": "USD",
  "billingCycle": "MONTHLY"
}
```

#### Downgrade to Free
```http
POST /api/conversions/downgrade
Authorization: Bearer {token}
```

### Analytics

#### Get Subscription Metrics
```http
GET /api/analytics/subscriptions
Authorization: Bearer {token}
```

#### Get Revenue Metrics (Premium Only)
```http
GET /api/analytics/revenue
Authorization: Bearer {token}
```

#### Get User Engagement Metrics
```http
GET /api/analytics/engagement
Authorization: Bearer {token}
```

#### Get Conversion Metrics (Premium Only)
```http
GET /api/analytics/conversions
Authorization: Bearer {token}
```

## Database Schema

The application uses the following main entities:

- **User**: User accounts with tier information (FREE/PREMIUM)
- **Subscription**: Subscription records with status and approval workflow
- **Transaction**: Payment and transaction history
- **Reminder**: Notification reminders for users

## Features

### Free Tier Features
- User registration and authentication
- View own subscriptions
- View transaction history
- Manage reminders
- Basic analytics

### Premium Tier Features
- All Free tier features
- Advanced analytics (revenue, conversion metrics)
- Priority support

### Admin Features
- Flag suspicious subscriptions
- Approve/reject subscriptions
- View all users and subscriptions

## Development

### Project Structure
```
src/
├── config/          # Configuration files
├── controllers/     # Request handlers
├── middleware/      # Express middleware
├── routes/          # API routes
├── services/        # Business logic
├── types/           # TypeScript types
└── utils/           # Utility functions
```

### Scripts
- `npm run dev` - Start development server with hot reload
- `npm run build` - Build for production
- `npm start` - Start production server
- `npm run prisma:generate` - Generate Prisma client
- `npm run prisma:migrate` - Run database migrations

## License

ISC
