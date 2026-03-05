# 1. Project Title

**Sub Tracker Backend API**

# 2. Overview

> [!NOTE]
> The architecture, codebase, and business logic of this application were designed and coded 100% by Artificial Intelligence (AI) tools.

**Sub Tracker Backend** is the backend service of the subscription and regular expense management application. It securely stores user data, handles premium subscription management, and provides a REST-based API.

# 3. Features

- **Authentication:** Secure encryption with BCrypt and a 7-day valid JWT-based authorization infrastructure.
- **Secure Data Management:** Secure centralized management of user and subscription data.
- **Role and Tier System:** Authorization restrictions for Free and Premium user tiers.

# 4. Tech Stack

The backend services are built with modern and high-performance tools:

- **Core:** Java 17, Spring Boot 3.2.1
- **Security:** Spring Security, JWT (JJWT 0.12.3), BCrypt
- **Database:** Spring Data MongoDB, MongoDB Cloud (Atlas)
- **API Documentation:** Swagger/OpenAPI
- **Dependency Management:** Maven, Lombok

# 5. Architecture

**N-Layered Architecture:**
The application is primarily divided into 3 main layers:
- **Controller Layer:** The layer where REST API endpoints are exposed and HTTP requests are handled.
- **Service Layer:** The layer containing the Business Logic. Designed to minimize dependency in architecture.
- **Repository Layer:** The layer managing communication with the MongoDB database.

This structure maximizes the sustainability, maintenance, and testability of the project.

# 6. Project Structure

The basic directory structure of the project:

```
src/main/java/com/sd/backend/
├── controller/      # REST Controller Classes (e.g., AuthController, SubscriptionController)
├── service/        # Business classes (e.g., AuthService)
├── repository/     # MongoDB Repository Classes
├── model/          # Database Entity Models (MongoDB Documents)
├── dto/            # HTTP Request/Response transfer objects
├── security/       # JWT configurations and Spring Security settings
└── exception/      # Global and custom error handling mechanisms
```

# 7. Installation / Setup

To start the API service in a local environment:

1. Install and compile dependencies:
   ```bash
   mvn clean package
   ```
2. Run the application:
   ```bash
   java -jar target/sd-backend-1.0.0.jar
   # or
   mvn spring-boot:run
   ```
3. The API will run at `http://localhost:8080` by default.
4. For Swagger interface: `http://localhost:8080/swagger-ui.html`

# 8. Configuration

Configuration settings can be made via `src/main/resources/application.properties` or environment variables:

- **MongoDB Connection:**
  ```properties
  spring.data.mongodb.uri=mongodb+srv://<username>:<password>@cluster.mongodb.net/?retryWrites=true&w=majority
  spring.data.mongodb.database=sd_backend
  ```
- **Authentication:** For your security, update the `jwt.secret` environment variable with a key of at least 32 characters.
- **CORS Settings:** Check the `cors.allowed.origins` key to allow your client's port.

# 9. Screenshots / UI

As a backend service, there is no visual graphical interface. However, with the interactive Swagger API documentation panel accessible at `http://localhost:8080/swagger-ui.html`, you can test all endpoints and view sample request-response schemas.

# 10. Monetization

The backend service defines the **Premium** membership status coming from the client (Android, etc.) and sets limits accordingly. When the "In-App Purchase" process on the client side is successfully transmitted, the user's authorization level is upgraded by the backend (e.g., Free -> Premium).

# 11. Backend Integration

Communication rules for client applications with the backend API:
- Requests and responses are in `application/json` format.
- All requests except `/api/auth/register` and `/api/auth/login` endpoints require a valid JWT token.
- Token transmission via HTTP header in requests: `Authorization: Bearer <token_value>`.

# 12. Testing

Test coverage for a stable API structure is ongoing:
- **Unit Testing:** Core business logic functions and service operations are tested using JUnit and Mockito libraries to ensure stability. It is in the process of being included in continuous integration.

# 13. Roadmap

Some important additions planned for the future:
- More advanced user dashboard configuration.
- Increasing response stability by including a Caching system with Redis.
- Adding comprehensive third-party payment infrastructure (e.g., Stripe) webhook integrations.

# 14. Contributing

If you want to contribute to the project, add a feature, or fix a bug:
1. Fork the project to your own profile.
2. Create a new branch (`git checkout -b feature/NewFeature`).
3. Complete your changes (`git commit -m 'New Feature: xyz'`).
4. Push your development to your repository (`git push origin feature/NewFeature`).
5. Then submit a Pull Request.

# 15. License

Copyright (c) 2026 Gökhan Aytekin

All rights reserved.

This repository is shared publicly for viewing and educational purposes only.

You may NOT:
- use this code in production
- copy significant parts of it
- redistribute it
- modify and distribute it

without explicit written permission from the author.

If you would like to use this code, please contact the author.

# 16. Contact

For bug reporting or collaboration:
- You can create a tracking card from the "Issues" section of the repository.
- Or you can contact the organizers via their contact addresses.
