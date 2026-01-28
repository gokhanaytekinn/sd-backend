# sd-backend

Backend API for Subscription Management - **Java Spring Boot 3.2 with Java 17**

## Overview

Production-ready Java 17 Spring Boot backend providing comprehensive APIs for subscription management, product analysis, and data analytics. Supports Free/Premium tiers with JWT authentication, suspicious activity detection, and analytics.

## Tech Stack

- **Java 17** | **Spring Boot 3.2.1** | **Spring Security** | **Spring Data JPA**
- **PostgreSQL** | **JWT (JJWT 0.12.3)** | **Maven** | **Lombok**

## Quick Start

```bash
# Build
mvn clean package

# Run
java -jar target/sd-backend-1.0.0.jar

# Or use Maven
mvn spring-boot:run
```

Access at: `http://localhost:8080`

## Configuration

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sd_backend
spring.datasource.username=postgres
spring.datasource.password=your_password

jwt.secret=your-secret-key-at-least-32-characters
jwt.expiration=604800000
```

## API Endpoints

**Auth** (`/api/auth`): register, login, /me  
**Subscriptions** (`/api/subscriptions`): CRUD, suspicious flagging, approval  
**Transactions** (`/api/transactions`): paginated history  
**Reminders** (`/api/reminders`): CRUD notifications  
**Conversions** (`/api/conversions`): upgrade/downgrade  
**Analytics** (`/api/analytics`): metrics & reports  

## Example Usage

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass123","name":"John"}'

# Login (get JWT token)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass123"}'

# Create Premium Subscription
curl -X POST http://localhost:8080/api/subscriptions \
  -H "Authorization: ******" \
  -H "Content-Type: application/json" \
  -d '{"tier":"PREMIUM","amount":9.99,"currency":"USD"}'
```

## Project Structure

```
com.sd.backend/
├── controller/      # 6 REST Controllers (24 endpoints)
├── service/        # 6 Service classes
├── repository/     # 4 JPA Repositories
├── model/          # 4 Entities + 5 Enums
├── dto/            # 13 DTOs
├── security/       # JWT + Spring Security Config
└── exception/      # Global error handling
```

## Features

✅ JWT Authentication (7-day expiration)  
✅ BCrypt Password Hashing  
✅ Subscription Management (Free/Premium)  
✅ Suspicious Activity Detection & Approval  
✅ Transaction History (Paginated)  
✅ Reminder System  
✅ User Tier Conversions  
✅ Analytics & Metrics  
✅ Global Exception Handling  
✅ Bean Validation  

## Database Schema

- **User**: Authentication, profile, tier (FREE/PREMIUM)
- **Subscription**: Status, billing, approval workflow  
- **Transaction**: Payment history with types & status
- **Reminder**: Scheduled notifications

All entities use UUID primary keys and include audit timestamps.

## Development

```bash
# Build
mvn clean install

# Run tests
mvn test

# Package
mvn clean package
```

## License

ISC

---

Built with ☕ Java 17 & Spring Boot 3.2
