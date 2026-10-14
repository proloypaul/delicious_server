<div align="center">

# 🍔 Delicious Server

**High-Performance, Scalable, Production-Ready Food Delivery REST API & Microservice Backend**

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT%20%2B%20HttpOnly%20Cookies-green?style=flat-square&logo=springsecurity)](https://spring.io/projects/spring-security)
[![Redis](https://img.shields.io/badge/Redis-7.x%20Lettuce%20Distributed%20Cache%20%26%20RateLimit-red?style=flat-square&logo=redis)](https://redis.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-blue?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations%20(V1--V3)-critical?style=flat-square&logo=flyway)](https://flywaydb.org/)
[![Swagger](https://img.shields.io/badge/OpenAPI%203-Swagger%20UI-brightgreen?style=flat-square&logo=swagger)](http://localhost:9080/swagger-ui.html)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

</div>

---

## 📑 Table of Contents

- [Overview & Architecture](#-overview--architecture)
  - [System Architecture Diagram](#system-architecture-diagram)
  - [Scalability & Million-User Readiness](#scalability--million-user-readiness)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [Security, Authentication & Cookies](#-security-authentication--cookies)
  - [Dual-Token Flow (Access + Refresh Token)](#dual-token-flow-access--refresh-token)
  - [Environment-Specific Cookie Security](#environment-specific-cookie-security)
  - [Distributed Login Rate Limiter](#distributed-login-rate-limiter)
- [Project Directory Structure](#-project-directory-structure)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Running Redis with Docker Compose](#running-redis-with-docker-compose)
  - [Configuration & Environment Profiles](#configuration--environment-profiles)
  - [Running the Server](#running-the-server)
- [API Reference](#-api-reference)
  - [Response Envelope Standard](#response-envelope-standard)
  - [1. Authentication & Session (`/api/v1/auth`)](#1-authentication--session-apiv1auth)
  - [2. User Management (`/api/v1/users`)](#2-user-management-apiv1users)
  - [3. Customer Operations (`/api/v1/customers`)](#3-customer-operations-apiv1customers)
  - [4. Seller Operations (`/api/v1/sellers`)](#4-seller-operations-apiv1sellers)
  - [5. Rider Dispatch & Delivery (`/api/v1/riders`)](#5-rider-dispatch--delivery-apiv1riders)
  - [6. Products & Menu Management (`/api/v1/products`)](#6-products--menu-management-apiv1products)
  - [7. Cuisine Categories (`/api/v1/categories`)](#7-cuisine-categories-apiv1categories)
  - [8. Orders & Processing Flow (`/api/v1/orders`)](#8-orders--processing-flow-apiv1orders)
  - [9. Reviews & Customer Feedback (`/api/v1/reviews`)](#9-reviews--customer-feedback-apiv1reviews)
  - [10. Promo Coupons Management (`/api/v1/coupons`)](#10-promo-coupons-management-apiv1coupons)
  - [11. Admin Intelligence & Metrics (`/api/v1/admin`)](#11-admin-intelligence--metrics-apiv1admin)
  - [12. Actuator & Observability (`/actuator`)](#12-actuator--observability-actuator)
- [Database Schema & Migrations](#-database-schema--migrations)
- [Default Seed Data](#-default-seed-data)
- [Global Error Handling](#-global-error-handling)
- [Testing & Quality Assurance](#-testing--quality-assurance)

---

## 🌟 Overview & Architecture

**Delicious Server** is the enterprise-grade backend powering the **Delicious** food delivery platform. Designed using **Domain-Driven Design (DDD)** and **Clean Layered Architecture**, it delivers a sub-millisecond, highly secure, and resilient experience supporting four core business personas:
- 🍽️ **Customers**: Browse menus, filter dishes, redeem promo codes, place real-time orders, track delivery stages, and post customer reviews.
- 🏪 **Sellers / Kitchens**: Manage multi-category product catalogs, toggle kitchen item stock, and monitor orders.
- 🛵 **Riders**: Claim ready orders, track pick-ups and drops, and advance order state machine.
- 🛡️ **Admins**: Platform-wide controls, role analytics, coupon creation, and single-pass aggregations.

### System Architecture Diagram

```
[ Next.js 15 Frontend / Mobile App ]
                 │ (HTTPS + Credentials / HttpOnly Cookies)
                 ▼
       [ Nginx / Reverse Proxy ]
                 │ (Forwarding Real Client IPs)
                 ▼
     [ Delicious Server (Spring Boot 3.4) ]
     ├── RateLimiterService ──► [ Redis 7 Cache & Atomic Keys ] (Fail-safe Fallback)
     ├── JwtAuthFilter (HttpOnly Access & Refresh Token verification)
     ├── SecurityFilterChain (Stateless RBAC: ADMIN, SELLER, RIDER, CUSTOMER)
     ├── Spring MVC Domain Controllers (Paginated, Validated)
     └── Spring Data JPA (Hibernate 6)
                 │
                 ▼ (Connection Pool HikariCP + Indexes)
         [ PostgreSQL 16+ ]
```

### Scalability & Million-User Readiness
1. **Resilient Non-Blocking Redis Layer:**
   - Dedicated `RedisConfig` with Lettuce client timeouts (`1500ms` command, `1000ms` connect).
   - Eager connection validation is turned off (`validateConnection(false)`). If Redis is temporarily stopped or unavailable, the server **never crashes**; it logs an informative warning and seamlessly defaults to an in-memory concurrent sliding-window rate limiter.
2. **Elimination of N+1 Queries & Unoptimized Counts:**
   - Admin stats aggregation (`AdminServiceImpl`) uses SQL conditional aggregation (`CASE WHEN`) and DB-level projections (`AdminUserCountProjection`, `AdminOrderStatsProjection`), replacing 8 separate count queries with single-pass executions.
3. **Optimized Database Schema & Composite Indexes (Flyway `V1` – `V3`):**
   - High-cardinality indexing on `user_id`, `seller_id`, `category_id`, `order_status`, `created_at`, and `code`.
4. **Stateless Scale-Out:**
   - Stateless JWT tokens stored in HttpOnly cookies enable horizontal auto-scaling across multiple container instances without sticky sessions.

---

## ✨ Key Features

| Feature | Production Architecture & Details |
|---|---|
| 🔐 **Stateless Dual-Token Auth** | Short-lived Access Token (15 min) + Long-lived Refresh Token (7 days) with automated server-side rotation |
| 🍪 **HttpOnly Cookie Protection** | Cookie injection on backend (`SameSite`, `HttpOnly`, `Path=/`, HTTPS in Prod); immunity against XSS script extraction |
| ⏱️ **Distributed Rate Limiting** | Strict IP-based login limit (3 attempts per 2 min) using Redis atomic keys + HTTP `429 Too Many Requests` + `Retry-After` header |
| 🛡️ **Role-Based Security (RBAC)** | Declarative `@PreAuthorize("hasRole('ADMIN')")`, `@PreAuthorize("hasAnyRole(...)")` across all endpoints |
| 🎟️ **Promo Coupons Engine** | Promo code validation, minimum order criteria, discount deductions, and admin coupon generation |
| ⭐ **Customer Reviews & Ratings** | Batch-enriched reviews with Spring Data pagination (`Page<ReviewResponse>`) and star rating breakdowns |
| 🚚 **Order State Machine** | Strict state lifecycle: `PENDING → PROCESSING → ACCEPTED_BY_RIDER → PICKED_UP → DELIVERED` |
| 📊 **Admin Dashboard Intelligence**| Single-query consolidated stats for users, orders, revenue, and product counts |
| 📄 **Universal Pagination** | All listing endpoints enforce Pageable contracts (`content`, `pageable`, `totalElements`, `totalPages`) |
| 🏥 **Full Observability** | Spring Actuator health probes and Prometheus-compatible metrics |

---

## 🛠 Technology Stack

| Component | Technology | Version | Purpose |
|---|---|---|---|
| **Language** | Java | 17 (LTS) | Modern OOP features, records, pattern matching |
| **Framework** | Spring Boot | 3.4.1 | Core application framework & dependency injection |
| **Security** | Spring Security | 6.4.x | Stateless security filter chains and RBAC |
| **JWT** | JJWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) | 0.12.6 | Cryptographic signing & parsing of HMAC-SHA512 tokens |
| **Caching & In-Memory**| Spring Data Redis + Lettuce | 3.4.1 | Distributed counters, distributed caching, fail-safe fallback |
| **Database** | PostgreSQL | 16+ | Primary relational transactional database |
| **ORM / Persistence** | Spring Data JPA / Hibernate | 6.x | Entity mapping, JPA specifications, Criteria queries |
| **Schema Migration** | Flyway Core + Flyway PostgreSQL | 10.x | Deterministic, audited DB versioning (`V1`, `V2`, `V3`) |
| **API Documentation** | SpringDoc OpenAPI | 2.7.0 | Swagger UI and OpenAPI 3 schema generation |
| **Productivity** | Lombok | 1.18.x | Boilerplate removal (`@Getter`, `@RequiredArgsConstructor`) |
| **Monitoring** | Spring Boot Actuator | 3.4.1 | Production readiness, liveness and readiness health checks |

---

## 🔒 Security, Authentication & Cookies

### Dual-Token Flow (Access + Refresh Token)

```
[ Client ]                             [ Delicious Server ]
    │                                          │
    │ ─── 1. POST /api/v1/auth/login ────────► │ Check Rate Limit (IP)
    │                                          │ Verify BCrypt Password
    │ ◄── 2. 200 OK + Set-Cookie (Access+Ref)─ │ Return AuthResponse
    │                                          │
    │ ─── 3. Authenticated Request (Cookie) ──► │ JwtAuthFilter validates Access Token
    │ ◄── 4. Protected Data ────────────────── │ If Expired: 401 Unauthorized
    │                                          │
    │ ─── 5. POST /api/v1/auth/refresh ───────► │ Validates Refresh Token (Cookie/Body)
    │ ◄── 6. 200 OK + Fresh Set-Cookie ─────── │ Issues New Access & Refresh Tokens
```

### Environment-Specific Cookie Security

Configured dynamically through [CookieService.java](file:///run/media/utsha/New%20Volume1/varsityAssignment/varsityProject/delicious_server/src/main/java/com/delicious/common/security/CookieService.java):

| Profile | `cookie.secure` | `cookie.same-site` | `cookie.domain` | HttpOnly | Description |
|---|---|---|---|---|---|
| **dev** | `false` | `Lax` | `""` (host only) | `true` | Allows local development across ports (e.g., frontend 3000 -> backend 9080) |
| **prod** | `true` | `None` / `Strict` | Environment-specific | `true` | Enforces HTTPS transmission; inaccessible to JavaScript `document.cookie` |

### Distributed Login Rate Limiter

Configured in [RateLimiterService.java](file:///run/media/utsha/New%20Volume1/varsityAssignment/varsityProject/delicious_server/src/main/java/com/delicious/common/security/RateLimiterService.java):
- **Rule**: Maximum **3 attempts** within **120 seconds** (2 minutes) per client IP.
- **Client IP Resolution**: Resolves original client IPs through `CF-Connecting-IP`, `X-Forwarded-For`, and `X-Real-IP`.
- **Redis Strategy**: Atomic Redis `INCR` key with sliding `EXPIRE` window: `rate_limit:login:ip:{clientIp}`.
- **Fail-Safe In-Memory Fallback**: If Redis connectivity fails, the service catches the error and switches to an in-memory `ConcurrentHashMap` with TTL verification, preventing denial of service to your authentication cluster.
- **Response On Violation**:
  - HTTP Status: `429 Too Many Requests`
  - Header: `Retry-After: <remaining-seconds>`
  - Message: *"Too many login attempts. You have exceeded the limit of 3 attempts in 2 minutes. Please wait 1 minute 45 seconds before trying again."*
- **Auto-Reset**: Successfully logging in immediately clears the counter for that IP.

---

## 📁 Project Directory Structure

```
delicious_server/
├── redis-compose.yaml                        # Standalone Docker compose for Redis 7 Alpine
├── pom.xml                                   # Project Maven build manifest & dependencies
└── src/main/
    ├── java/com/delicious/
    │   ├── DeliciousServerApplication.java   # Main Spring Boot Application Bootstrap
    │   ├── common/                           # Cross-cutting enterprise infrastructure
    │   │   ├── config/                       # Spring Beans: RedisConfig, SwaggerConfig, JpaConfig, DataInitializer
    │   │   ├── constants/                    # Global constants
    │   │   ├── entity/                       # BaseEntity (createdAt, updatedAt @MappedSuperclass)
    │   │   ├── exception/                    # GlobalExceptionHandler, RateLimitExceededException, ResourceNotFoundException
    │   │   ├── response/                     # Standard ApiResponse<T> envelope wrapper
    │   │   ├── security/                     # SecurityConfig, JwtService, JwtAuthFilter, CookieService, RateLimiterService
    │   │   └── util/                         # Helper utilities
    │   │
    │   └── domain/                           # DDD Modular Subsystems
    │       ├── admin/                        # Admin metrics, aggregated projections & controller
    │       ├── coupon/                       # Promo coupon entity, repository, validation service & admin controller
    │       ├── customer/                     # Customer profile entities, repository, and controller
    │       ├── order/                        # Order state machine, OrderItems, checkout & tracking
    │       ├── product/                      # Product catalog, multi-category mappings, search & approval status
    │       ├── review/                       # Customer product reviews, ratings & batch-enriched queries
    │       ├── rider/                        # Rider fleet onboarding, delivery assignment & updates
    │       ├── seller/                       # Seller profile, kitchen details & store menu endpoints
    │       └── user/                         # Core Auth, User repository, Login, Register, Refresh Token
    │
    └── resources/
        ├── application.yml                   # Root profile selector (active: dev)
        ├── application-dev.yml               # Dev profile (Postgres localhost, Redis 6379, Lax cookies)
        ├── application-prod.yml              # Production profile (Environment variable bindings)
        └── db/migration/
            ├── V1__init.sql                  # Initial baseline tables
            ├── V2__add_indexes_and_constraints.sql  # High-throughput foreign keys & query indexes
            └── V3__add_coupons_table.sql     # Promo coupon schema & indexing
```

---

## 🚀 Getting Started

### Prerequisites

- **Java JDK 17** or higher
- **Apache Maven 3.8+**
- **PostgreSQL 16+**
- **Docker & Docker Compose** (for Redis)

### Running Redis with Docker Compose

A dedicated `redis-compose.yaml` is provided:

```bash
# Start Redis in the background
docker compose -f redis-compose.yaml up -d

# Verify Redis is running
docker compose -f redis-compose.yaml ps
```

### Configuration & Environment Profiles

#### Development (`application-dev.yml`)
- PostgreSQL default: `jdbc:postgresql://localhost:5432/deliciousdb`
- Redis default: `localhost:6379`
- Server port: `9080`

#### Production Environment Variables (`application-prod.yml`)
| Variable | Description | Example |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://db.prod:5432/deliciousdb` |
| `DB_USERNAME` | Database username | `postgres_prod` |
| `DB_PASSWORD` | Database password | `StrongSecretPassword` |
| `REDIS_HOST` | Redis host or cluster endpoint | `redis-cluster.internal` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis authentication password | `RedisSecretPassword` |
| `JWT_SECRET` | 512-bit Base64-encoded secret key | *(generate via `openssl rand -base64 64`)* |
| `COOKIE_SECURE` | Set `true` for HTTPS in production | `true` |
| `COOKIE_SAME_SITE` | SameSite cookie policy | `None` (cross-domain) or `Lax` |
| `ALLOWED_ORIGINS`| CORS allowed frontend origins | `https://delicious.yourdomain.com` |

### Running the Server

```bash
# 1. Compile and package the application
mvn clean package -DskipTests

# 2. Run in Development profile
mvn spring-boot:run

# 3. Or launch with Production profile
java -jar target/delicious-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Interactive OpenAPI Swagger UI: [`http://localhost:9080/swagger-ui.html`](http://localhost:9080/swagger-ui.html)  
Health Check: [`http://localhost:9080/actuator/health`](http://localhost:9080/actuator/health)

---

## 📡 API Reference

### Response Envelope Standard

Every API response follows a consistent wrapper format:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

---

### 1. Authentication & Session (`/api/v1/auth`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/register` | Public | Register customer account, hash password, sets auth cookies |
| `POST` | `/login` | Public (Rate-Limited) | Authenticates user; enforces 3 attempts/2 min limit; sets `access_token` & `refresh_token` cookies |
| `POST` | `/refresh-token` | Public | Rotates tokens using HttpOnly refresh cookie or body payload |
| `POST` | `/logout` | Authenticated | Clears `access_token`, `refresh_token`, and `user_role` cookies |

#### Login Request Body
```json
{
  "email": "customer@delicious.com",
  "password": "securePassword123"
}
```

---

### 2. User Management (`/api/v1/users`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/` | Admin | Get paginated list of all registered users (`?page=0&size=20`) |
| `GET` | `/{id}` | Admin / User | Get specific user by primary ID |
| `PATCH` | `/{id}/status` | Admin | Update user status (`ACTIVE`, `INACTIVE`, `BANNED`) |

---

### 3. Customer Operations (`/api/v1/customers`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/register` | Public | Register customer profile |
| `GET` | `/profile?userId={id}` | Authenticated | Fetch customer profile data & address |
| `PUT` | `/profile?userId={id}` | Authenticated | Update name, phone, delivery address |
| `GET` | `/` | Admin | Paginated list of all customer records |

---

### 4. Seller Operations (`/api/v1/sellers`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/register` | Public | Onboard new seller kitchen with `storeName` |
| `GET` | `/profile?userId={id}` | Authenticated | Get seller profile, store details, and status |
| `PUT` | `/profile?userId={id}` | Seller / Admin | Update seller details |
| `GET` | `/products?userId={id}` | Public | List all dishes prepared by a specific seller |
| `GET` | `/` | Admin | Get paginated directory of registered sellers |

---

### 5. Rider Dispatch & Delivery (`/api/v1/riders`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/` or `/register` | Public | Onboard rider with `vehicleType` and `vehicleRegistrationNumber` |
| `GET` | `/` | Admin | List all riders (paginated) |
| `GET` | `/{id}` | Authenticated | Get rider profile details |
| `GET` | `/orders/{riderId}` | Rider / Admin | Get list of assigned delivery orders |
| `PUT` | `/status/{orderId}?riderId={id}`| Rider / Admin | Update delivery stage (`ACCEPTED_BY_RIDER`, `PICKED_UP`, `DELIVERED`) |

---

### 6. Products & Menu Management (`/api/v1/products`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/` | Public | Paginated product list with optional category filter (`?categoryName=Burger&page=0&size=10`) |
| `GET` | `/{id}` | Public | Detailed view of a single product |
| `POST` | `/` | Seller / Admin | Add new food item to kitchen menu |
| `PUT` | `/{id}` | Seller / Admin | Update price, preparation time, description, or status |
| `DELETE` | `/{id}` | Seller / Admin | Delete product from catalog |

#### Create Product Payload
```json
{
  "foodName": "Truffle Beef Burger",
  "description": "Double beef patty with truffle mayo and aged cheddar",
  "price": 12.99,
  "makingTime": 20,
  "status": "APPROVED",
  "sellerId": 2,
  "categoryId": 1
}
```

---

### 7. Cuisine Categories (`/api/v1/categories`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/` | Public | List all cuisine categories (paginated, ordered by name) |
| `GET` | `/{id}` | Public | Get single category |
| `POST` | `/` | Admin | Create category with duplicate name check |
| `PUT` | `/{id}` | Admin | Update category name and image |
| `DELETE` | `/{id}` | Admin | Remove category |

---

### 8. Orders & Processing Flow (`/api/v1/orders`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/?customerId={id}` | Customer | Place order with cart items, address, and coupon discount |
| `GET` | `/{id}` | Authenticated | Inspect order details and order item breakdown |
| `GET` | `/customer?customerId={id}` | Customer | View customer order history |
| `GET` | `/` | Admin | View platform-wide orders (paginated) |
| `PUT` | `/status` | Admin / Seller | Progress order state machine |

#### Order Flow Lifecycle
```
PENDING ──► PROCESSING ──► ACCEPTED_BY_RIDER ──► PICKED_UP ──► DELIVERED
```

---

### 9. Reviews & Customer Feedback (`/api/v1/reviews`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/` | Customer | Submit product review (Rating: 1 to 5) |
| `GET` | `/product/{id}` | Public | Paginated reviews for a product (`?page=0&size=10&sort=createdAt,desc`) with batch-enriched customer names |

#### Review Submission Payload
```json
{
  "productId": 5,
  "customerId": 3,
  "rating": 5,
  "message": "Spectacular flavor, arrived hot and crispy!"
}
```

---

### 10. Promo Coupons Management (`/api/v1/coupons`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/validate` | Public | Validate promo code against current cart total (`?code=SAVE20&orderAmount=45.00`) |
| `GET` | `/` | Admin | Paginated list of all promotional coupons (`?page=0&size=20`) |
| `POST` | `/` | Admin | Create new coupon with discount amount & min order value |

---

### 11. Admin Intelligence & Metrics (`/api/v1/admin`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/dashboard/stats` | Admin | Consolidated dashboard metrics in 3 optimized queries (users by role, orders by status, total revenue, total products) |

#### Sample Dashboard Stats Response
```json
{
  "success": true,
  "message": "Dashboard statistics retrieved successfully",
  "data": {
    "totalCustomers": 1240,
    "totalSellers": 48,
    "totalRiders": 85,
    "totalProducts": 630,
    "totalOrders": 8940,
    "pendingOrders": 12,
    "deliveredOrders": 8520,
    "totalRevenue": 142580.50
  }
}
```

---

### 12. Actuator & Observability (`/actuator`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/actuator/health` | Comprehensive application health status including DB and Redis state |
| `GET` | `/actuator/metrics` | JVM, thread pool, HikariCP, and HTTP request metrics |

---

## 🗄️ Database Schema & Migrations

Database evolution is managed via **Flyway** under `src/main/resources/db/migration/`:

| Version | Script | Changes & Optimizations |
|---|---|---|
| `V1` | `V1__init.sql` | Base schema: `users`, `customers`, `sellers`, `riders`, `categories`, `products`, `orders`, `order_items`, `reviews` |
| `V2` | `V2__add_indexes_and_constraints.sql` | Added composite indexes (`idx_products_seller_id`, `idx_orders_status`, `idx_reviews_product_id`, etc.) and foreign key cascades |
| `V3` | `V3__add_coupons_table.sql` | Created `coupons` table with unique uppercase code constraint and index on `code` |

---

## 🌱 Default Seed Data

On startup, `DataInitializer` seeds test accounts if they do not exist:

| Persona | Email | Default Password | Role |
|---|---|---|---|
| **Admin** | `admin@example.com` | `password123` | `ADMIN` |
| **Customer** | `john@example.com` | `password123` | `CUSTOMER` |
| **Seller** | `alice@example.com` | `password123` | `SELLER` |
| **Rider** | `rider@example.com` | `password123` | `RIDER` |

> ⚠️ **Important:** In production environments, replace default credentials and configure environment-supplied secrets immediately.

---

## 🚨 Global Error Handling

All controller errors and edge cases are captured centrally in [GlobalExceptionHandler.java](file:///run/media/utsha/New%20Volume1/varsityAssignment/varsityProject/delicious_server/src/main/java/com/delicious/common/exception/GlobalExceptionHandler.java):

```json
{
  "success": false,
  "message": "Too many login attempts. You have exceeded the limit of 3 attempts in 2 minutes. Please wait 1 minute 45 seconds before trying again.",
  "data": null
}
```

| HTTP Status | Condition / Exception |
|---|---|
| `400 Bad Request` | `@Valid` body validation errors, malformed parameters |
| `401 Unauthorized` | Invalid username/password, expired access token |
| `403 Forbidden` | Access denied (insufficient role privileges) |
| `404 Not Found` | Entity not found (`ResourceNotFoundException`, `EntityNotFoundException`) |
| `409 Conflict` | Duplicate record (`UserAlreadyExistsException`, `ResourceAlreadyExistsException`) |
| `429 Too Many Requests` | `RateLimitExceededException` (with `Retry-After` header) |
| `500 Internal Error` | Sanitized server error message (internal stack traces never exposed) |

---

## 🧪 Testing & Quality Assurance

```bash
# Execute unit and repository tests
mvn clean test

# Run tests and generate verification reports
mvn clean verify
```

---

<div align="center">

**Delicious Server — Built with ❤️ for High-Scale Food Delivery**

</div>
