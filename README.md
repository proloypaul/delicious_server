<div align="center">

# 🍔 Delicious Server

**A robust, production-ready RESTful backend API for a modern food delivery platform**

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-blue?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-migrations-red?style=flat-square&logo=flyway)](https://flywaydb.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

</div>

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Configuration](#configuration)
  - [Running the Application](#running-the-application)
- [API Reference](#-api-reference)
  - [Authentication](#authentication-apiv1auth)
  - [Users](#users-apiv1users)
  - [Customers](#customers-apiv1customers)
  - [Sellers](#sellers-apiv1sellers)
  - [Riders](#riders-apiv1riders)
  - [Products](#products-apiv1products)
  - [Categories](#categories-apiv1categories)
  - [Orders](#orders-apiv1orders)
  - [Reviews](#reviews-apiv1reviews)
  - [Admin](#admin-apiv1admin)
  - [Actuator](#actuator-actuator)
- [Data Models](#-data-models)
- [Database Migrations](#-database-migrations)
- [Seed Data](#-seed-data)
- [Error Handling](#-error-handling)
- [Running Tests](#-running-tests)

---

## 🌟 Overview

**Delicious Server** is the backend engine powering a full-stack food delivery application. It exposes a comprehensive REST API catering to four distinct user roles: **Customers**, **Sellers**, **Riders**, and **Admins**. The platform manages the entire lifecycle of a food order — from browsing menus and placing orders, to delivery and review.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔐 **Authentication** | User registration & login with BCrypt password hashing |
| 👤 **Multi-Role Users** | Distinct roles: `CUSTOMER`, `SELLER`, `RIDER`, `ADMIN` |
| 🏪 **Seller Management** | Seller registration, profile management, and product listing |
| 🛵 **Rider Management** | Rider onboarding, order assignment, and delivery status updates |
| 🛒 **Order Lifecycle** | Full order flow: `PENDING → PROCESSING → ACCEPTED_BY_RIDER → PICKED_UP → DELIVERED` |
| 🍕 **Product Catalogue** | Create, update, delete products with category filtering and pagination |
| 📦 **Category Management** | Full CRUD for product categories with duplicate name protection |
| ⭐ **Product Reviews** | Customers can submit and view product reviews with ratings |
| 📊 **Admin Dashboard** | Platform-wide statistics and metrics |
| 🛡️ **Error Handling** | Structured, client-safe error responses — no internal detail leakage |
| 📄 **Pagination** | All list endpoints support Spring Data `Pageable` with configurable page sizes |
| 🏥 **Health Checks** | Spring Actuator `/health` and `/metrics` endpoints for production monitoring |
| 🗄️ **DB Migrations** | Flyway for safe, versioned schema evolution |
| 📖 **API Documentation** | Interactive Swagger UI via SpringDoc OpenAPI |

---

## 🛠 Technology Stack

| Category | Technology |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.4.1 |
| **Security** | Spring Security with BCrypt |
| **Persistence** | Spring Data JPA + Hibernate 6 |
| **Database** | PostgreSQL 16+ |
| **DB Migration** | Flyway |
| **Build Tool** | Apache Maven |
| **API Docs** | SpringDoc OpenAPI 2.7 (Swagger UI) |
| **Monitoring** | Spring Boot Actuator |
| **Utilities** | Lombok |

---

## 📁 Project Structure

```
delicious_server/
│
├── src/main/
│   ├── java/com/delicious/
│   │   │
│   │   ├── DeliciousServerApplication.java       # Application entry point
│   │   │
│   │   ├── common/                               # Shared, cross-cutting components
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java          # Dev seed data runner
│   │   │   │   ├── JpaConfig.java                # JPA auditing configuration
│   │   │   │   └── SwaggerConfig.java            # OpenAPI/Swagger configuration
│   │   │   ├── entity/
│   │   │   │   └── BaseEntity.java               # Shared auditing fields (createdAt, updatedAt)
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java   # Centralized exception handling
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── ResourceAlreadyExistsException.java
│   │   │   ├── response/
│   │   │   │   └── ApiResponse.java              # Standardized API response wrapper
│   │   │   └── security/
│   │   │       ├── SecurityConfig.java           # Spring Security & CORS configuration
│   │   │       └── CustomUserDetailsService.java
│   │   │
│   │   └── domain/                               # Feature modules (Domain-Driven Design)
│   │       ├── admin/                            # Platform admin statistics
│   │       ├── customer/                         # Customer profiles
│   │       ├── order/                            # Order management
│   │       ├── product/                          # Products & categories
│   │       ├── review/                           # Product reviews
│   │       ├── rider/                            # Rider profiles & delivery
│   │       ├── seller/                           # Seller profiles
│   │       └── user/                             # Auth & user management
│   │           ├── controller/
│   │           ├── dto/
│   │           ├── entity/
│   │           ├── enums/
│   │           ├── exception/
│   │           ├── mapper/
│   │           ├── repository/
│   │           └── service/
│   │
│   └── resources/
│       ├── application.yml                       # Root config (sets active profile)
│       ├── application-dev.yml                   # Development configuration
│       ├── application-prod.yml                  # Production configuration (env vars)
│       └── db/migration/
│           └── V1__init.sql                      # Baseline Flyway migration
│
└── pom.xml
```

Each **domain module** follows a consistent layered structure:

```
domain/<feature>/
├── controller/    # REST endpoints
├── dto/           # Request & Response objects
├── entity/        # JPA entities
├── enums/         # Domain-specific enumerations
├── exception/     # Domain-specific exceptions
├── mapper/        # Entity <-> DTO conversion
├── repository/    # Spring Data JPA repositories
└── service/       # Business logic (Interface + Implementation)
```

---

## 🚀 Getting Started

### Prerequisites

- **JDK 17** or higher
- **Apache Maven 3.8+**
- **PostgreSQL 16+** running locally

### Configuration

**1. Create the database:**
```sql
CREATE DATABASE deliciousdb;
```

**2. Configure credentials** in `src/main/resources/application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/deliciousdb
    username: your_username
    password: your_password
```

**For Production**, environment variables are used instead of hardcoded credentials:

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | Full JDBC connection URL | `jdbc:postgresql://localhost:5432/deliciousdb` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | — |
| `PORT` | Server port | `9080` |
| `ALLOWED_ORIGINS` | Comma-separated CORS origins | `http://localhost:3000` |

### Running the Application

```bash
# Clone the repository
git clone https://github.com/proloypaul/delicious_server.git
cd delicious_server

# Run in development mode (uses application-dev.yml)
mvn spring-boot:run

# Or build and run the JAR
mvn clean package -DskipTests
java -jar target/delicious-server-0.0.1-SNAPSHOT.jar

# Switch to production profile
java -jar target/delicious-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

The server will start on **`http://localhost:9080`**.

📖 **Swagger UI** is available at: [`http://localhost:9080/swagger-ui.html`](http://localhost:9080/swagger-ui.html)

---

## 📡 API Reference

All endpoints are prefixed with `/api/v1`. All responses follow a standard wrapper format:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

---

### Authentication `/api/v1/auth`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/register` | Register a new user account |
| `POST` | `/login` | Authenticate and get user details |

**Register Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "01234567890",
  "password": "securePassword",
  "role": "CUSTOMER"
}
```

**Login Request Body:**
```json
{
  "email": "john@example.com",
  "password": "securePassword"
}
```

---

### Users `/api/v1/users`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/` | Get paginated list of all users |
| `GET` | `/{id}` | Get a specific user by ID |
| `PATCH` | `/{id}/status` | Update user active/inactive status |

---

### Customers `/api/v1/customers`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/profile?userId={id}` | Get a customer's profile |
| `PUT` | `/profile?userId={id}` | Update customer name, phone, or address |
| `GET` | `/` | Get all customers (paginated) |

---

### Sellers `/api/v1/sellers`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/register` | Register as a new seller |
| `GET` | `/profile?userId={id}` | Get a seller's profile |
| `PUT` | `/profile?userId={id}` | Update seller profile and store details |
| `GET` | `/products?userId={id}` | Get all products belonging to a seller |
| `GET` | `/` | Get all registered sellers (paginated) |

---

### Riders `/api/v1/riders`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/` | Register a new rider |
| `GET` | `/` | Get all riders (paginated) |
| `GET` | `/{id}` | Get a rider's profile by ID |
| `GET` | `/orders/{riderId}` | Get all orders assigned to a rider |
| `PUT` | `/status/{orderId}?riderId={id}` | Update the delivery status of an order |

---

### Products `/api/v1/products`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/` | Create a new product listing |
| `GET` | `/?categoryName={name}` | Get all products, optionally filtered by category (paginated) |
| `GET` | `/{id}` | Get a specific product by ID |
| `PUT` | `/{id}` | Update a product |
| `DELETE` | `/{id}` | Delete a product |

**Product Request Body:**
```json
{
  "foodName": "Double Cheese Burger",
  "description": "Delicious double cheese burger",
  "price": 5.99,
  "makingTime": 15,
  "status": "APPROVED",
  "sellerId": 2,
  "categoryId": 1
}
```

---

### Categories `/api/v1/categories`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/` | Create a new category |
| `GET` | `/` | Get all categories (paginated, sorted by name) |
| `GET` | `/{id}` | Get a specific category by ID |
| `PUT` | `/{id}` | Update a category |
| `DELETE` | `/{id}` | Delete a category |

---

### Orders `/api/v1/orders`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/?customerId={id}` | Place a new order |
| `GET` | `/{id}` | Get a specific order by ID |
| `GET` | `/customer?customerId={id}` | Get all orders for a specific customer |
| `GET` | `/` | Get all orders (admin view, paginated) |
| `PUT` | `/status` | Update the status of an order |

**Order Status Flow:**
```
PENDING → PROCESSING → ACCEPTED_BY_RIDER → PICKED_UP → DELIVERED
```

**Order Request Body:**
```json
{
  "phone": "01234567890",
  "address": "123 Main Street, City",
  "discount": 0.00,
  "items": [
    { "productId": 1, "quantity": 2 }
  ]
}
```

---

### Reviews `/api/v1/reviews`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/` | Submit a review for a product |
| `GET` | `/product/{id}` | Get all reviews for a product |

**Review Request Body:**
```json
{
  "productId": 1,
  "customerId": 3,
  "rating": 5,
  "message": "Absolutely delicious!"
}
```

---

### Admin `/api/v1/admin`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/dashboard/stats` | Get platform-wide statistics (users, orders, revenue) |

---

### Actuator `/actuator`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/actuator/health` | Application health status (for load balancers & probes) |
| `GET` | `/actuator/metrics` | Application performance metrics |

---

## 🗄️ Data Models

### User Roles

| Role | Description |
|---|---|
| `CUSTOMER` | Can browse products, place orders, and leave reviews |
| `SELLER` | Can manage their product catalogue |
| `RIDER` | Can view and fulfill assigned deliveries |
| `ADMIN` | Has platform-wide management access |

### User Status

| Status | Description |
|---|---|
| `ACTIVE` | User can log in and use the platform |
| `INACTIVE` | Account created but not yet activated |
| `BANNED` | Account is suspended |

### Product Status

| Status | Description |
|---|---|
| `PENDING` | Newly submitted product awaiting admin approval |
| `APPROVED` | Product is live and visible to customers |

### Order Status

| Status | Description |
|---|---|
| `PENDING` | Order placed, awaiting seller processing |
| `PROCESSING` | Seller is preparing the order |
| `ACCEPTED_BY_RIDER` | A rider has accepted the delivery |
| `PICKED_UP` | Rider has collected the order |
| `DELIVERED` | Order successfully delivered |

---

## 🗃️ Database Migrations

This project uses **Flyway** for versioned database schema management.

Migration scripts are located in `src/main/resources/db/migration/` and follow the naming convention: `V{version}__{description}.sql`.

| Version | Script | Description |
|---|---|---|
| `V1` | `V1__init.sql` | Baseline schema — all tables, constraints, indexes |

**Flyway is disabled in the `dev` profile by default** to allow Hibernate's `ddl-auto: validate` to manage schema during active development. To enable it:

```yaml
# application-dev.yml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
```

**To add a new migration**, create a new file following the version convention:
```
src/main/resources/db/migration/V2__add_column_to_products.sql
```

---

## 🌱 Seed Data

On application startup, `DataInitializer` automatically seeds the database with sample data if the records don't already exist (checks by email before inserting).

| Role | Email | Password |
|---|---|---|
| Admin | `admin@example.com` | `password123` |
| Customer | `john@example.com` | `password123` |
| Seller | `alice@example.com` | `password123` |
| Rider | `rider@example.com` | `password123` |

Additionally, a **"Fast Food"** category and a **"Double Cheese Burger"** product are seeded, linked to the Seller account.

> ⚠️ **Change default passwords immediately in any non-development environment.**

---

## 🚨 Error Handling

All errors return a structured response to avoid leaking internal system details:

```json
{
  "success": false,
  "message": "Resource not found: Category not found with id: 99",
  "data": null
}
```

| HTTP Status | Scenario |
|---|---|
| `400 Bad Request` | Validation failure on request body |
| `401 Unauthorized` | Invalid credentials |
| `403 Forbidden` | Access denied |
| `404 Not Found` | Requested resource does not exist |
| `405 Method Not Allowed` | HTTP method is not supported on this endpoint |
| `409 Conflict` | Resource already exists (e.g., duplicate email or category name) |
| `500 Internal Server Error` | Unexpected server-side error (message is sanitized — not leaked to client) |

---

## 🧪 Running Tests

```bash
# Run all tests
mvn clean test

# Run tests and generate a report
mvn clean verify
```

The test suite uses Spring Boot's integration testing support with a real PostgreSQL connection configured in `application.yml`. Ensure your local database is available before running tests.

---

## 📌 Project Disclaimer

> This project was developed as a **learning and academic exercise** to explore and demonstrate proficiency in building a domain-driven REST API using the Spring Boot ecosystem.

While the codebase reflects real-world structural patterns — including layered architecture, exception handling, database migrations, multi-profile configuration, and observability — it is **not intended for production deployment in its current state**.

In particular:

- **Authentication** is partially implemented. Password hashing with BCrypt is fully functional; however, **JWT token issuance and stateless session validation are not yet integrated**. User identity for protected endpoints is currently passed as a request parameter as a temporary measure during development.
- **Authorization (RBAC)** — role-based access control to restrict endpoints by user role — is planned but not enforced at this stage.

These aspects represent natural next steps in the evolution of the project, and the existing architecture has been designed to accommodate them cleanly.

---

<div align="center">

Built for learning purposes · Academic Practice Project

Made with ❤️ using Spring Boot

</div>
