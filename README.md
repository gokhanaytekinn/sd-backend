# sd-backend

Backend API for Subscription Management - **Java Spring Boot 3.2 with Java 17 & MongoDB**

## Overview

Production-ready Java 17 Spring Boot backend providing comprehensive APIs for subscription management, product analysis, and data analytics. Supports Free/Premium tiers with JWT authentication, suspicious activity detection, and analytics.

## Tech Stack

- **Java 17** | **Spring Boot 3.2.1** | **Spring Security** | **Spring Data MongoDB**
- **MongoDB Cloud** | **JWT (JJWT 0.12.3)** | **Maven** | **Lombok** | **Swagger/OpenAPI**

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

### 📚 API Documentation (Swagger UI)

Interactive API documentation is available at:
```
http://localhost:8080/swagger-ui.html
```

Features:
- 📖 Complete API documentation for all endpoints
- 🔐 Built-in JWT authentication testing
- ✅ Try out APIs directly from your browser
- 📝 Request/response examples

OpenAPI JSON specification:
```
http://localhost:8080/v3/api-docs
```

## MongoDB Configuration (wa-core style)

The application uses MongoDB with simple URI and database name configuration:

### Using Environment Variables (Recommended)
```bash
export MONGODB_URI="mongodb+srv://username:password@cluster.mongodb.net/?retryWrites=true&w=majority"
export MONGODB_DATABASE="sd_backend"
```

### Or edit `src/main/resources/application.properties`:
```properties
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/?retryWrites=true&w=majority
spring.data.mongodb.database=sd_backend
```

### Default Configuration (Local MongoDB)
If not configured, defaults to:
- URI: `mongodb://localhost:27017`
- Database: `sd_backend`

## Additional Configuration

Edit `src/main/resources/application.properties`:
```properties
# JWT Configuration
jwt.secret=your-secret-key-at-least-32-characters
jwt.expiration=604800000

# CORS
cors.allowed.origins=http://localhost:3000
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
├── repository/     # 4 MongoDB Repositories
├── model/          # 4 Documents + 5 Enums
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
✅ **Swagger/OpenAPI Documentation**  

## Database Schema

MongoDB Collections:
- **users**: Authentication, profile, tier (FREE/PREMIUM)
- **subscriptions**: Status, billing, approval workflow  
- **transactions**: Payment history with types & status
- **reminders**: Scheduled notifications

All documents use MongoDB auto-generated String IDs (ObjectId).

## Development

```bash
# Build
mvn clean install

# Run tests
mvn test

# Package
mvn clean package
```

## MongoDB Cloud Setup

1. Create a MongoDB Atlas account at https://www.mongodb.com/cloud/atlas
2. Create a new cluster
3. Get your connection string (URI)
4. Create a database named `sd_backend` (or use any name)
5. Set environment variables or update application.properties

## License

ISC

---

Built with ☕ Java 17 & Spring Boot 3.2 & 🍃 MongoDB
