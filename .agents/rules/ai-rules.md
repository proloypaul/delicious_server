---
trigger: always_on
---

# 01 — Architecture & Project Structure

**SCOPE**: This file defines the structural and architectural rules for the entire backend application.
All code — human-written or AI-generated — MUST comply. This is the structural foundation that all other rule files build upon.

---

## Architecture Model

**Style**: Modular Monolith with Feature-Based (Domain-Oriented) internal structure.
**Intra-domain pattern**: Layered — `Controller → Service → Repository`.
**Future-proofing**: Each domain MUST be designed to be extractable into an independent microservice without business logic changes.

```
Client
  │
  ▼
Security Filter (Spring Security / JWT)
  │
  ▼
Controller  ──→  [validates input via @Valid]
  │
  ▼
Service     ──→  [all business logic + @Transactional]
  │
  ▼
Repository  ──→  [data access only]
  │
  ▼
Database (PostgreSQL)
```

Response path (reverse):
```
DB → Entity → Mapper → DTO → Controller → HTTP Response
```

---

## R1 — Package Structure (Mandatory)

**Organize by domain (feature-first), never by technical layer.**

```
com.company.appname/
│
├── common/                        # Shared infrastructure — NO business logic
│   ├── config/                    # Spring configuration beans
│   ├── security/                  # JWT, filters, SecurityConfig
│   ├── exception/                 # GlobalExceptionHandler, domain exceptions
│   ├── response/                  # Standardized API response wrapper
│   ├── constants/                 # App-wide constants
│   └── util/                      # Pure utility/helper classes
│
├── domain/                        # All business domains
│   ├── user/
│   ├── participant/
│   ├── contest/
│   ├── submission/
│   └── payment/
│
└── ApplicationMain.java
```

**Each domain package MUST follow this internal structure:**

```
domain/{feature}/
├── controller/       # HTTP layer only
├── service/          # Interface + Impl — all business logic
├── repository/       # Spring Data interfaces only
├── entity/           # JPA entities
├── dto/              # Request and Response DTOs (separate classes)
├── mapper/           # Entity ↔ DTO mapping (Map manually)
├── enums/            # Domain-specific enums
├── event/            # Domain events (if used)
├── specification/    # JPA Specifications (optional, for dynamic queries)
└── projection/       # Interface or record projections (optional)
```

---

## R2 — Dependency Rules

### Allowed
| From | To | Notes |
|---|---|---|
| Controller | Service (interface) | Via interface, not impl |
| Service | Repository | Same domain only |
| Service | Service | Cross-domain via service interface only |
| Repository | Database | Spring Data — no raw JDBC unless required |

### Forbidden

| Dependency | Reason |
|---|---|
| `Controller → Repository` | Bypasses business logic layer |
| `Controller → Entity` | Exposes persistence model to HTTP layer |
| `Repository → Service` | Inverts the dependency direction |
| `DTO → Repository` | DTO is a transport object, not a data access actor |
| `Service → another domain's Repository` | Violates domain isolation — use the domain's Service |

---

## R3 — Layer Responsibilities (Strict)

### Controller
- Handles HTTP routing and delegates to Service.
- Validates input using `@Valid` on `@RequestBody` and `@PathVariable`/`@RequestParam` constraints.
- Returns a standardized response wrapper (e.g., `ApiResponse<T>`).
- MUST NOT contain: `if/else` business logic, database calls, entity manipulation.

### Service
- Contains ALL business logic. This is the only layer where decisions are made.
- Defines an interface (`XxxService`) + implementation (`XxxServiceImpl`).
- Owns transaction boundaries — apply `@Transactional` here, never in Controller or Repository.
- `@Transactional(readOnly = true)` on read methods for performance.
- May call other domain Services. MUST NOT call other domain Repositories.

### Repository
- Extends `JpaRepository<Entity, ID>` or `JpaSpecificationExecutor`.
- Contains only data access: queries, custom `@Query`, Specifications, Projections.
- MUST NOT contain: business logic, validation, mapping, service calls.

### Entity
- Annotated with `@Entity`. Maps directly to a database table.
- MUST NOT be returned from any Controller or Service method visible to the API.
- Include audit fields: `createdAt`, `updatedAt` (via `@CreatedDate`, `@LastModifiedDate` + `@EntityListeners(AuditingEntityListener.class)`).
- Use `@ManyToOne(fetch = FetchType.LAZY)` as default. Never `EAGER` on collection associations.

### DTO (Data Transfer Object)
- Separate classes for request and response: `CreateOrderRequest`, `OrderResponse`.
- Use Java `record` for immutable DTOs where applicable (Spring Boot 3+).
- Apply `jakarta.validation` constraints on request DTOs only (`@NotBlank`, `@NotNull`, `@Size`, `@Pattern`).
- MUST NOT contain JPA annotations or persistence references.

### Mapper
- Handles Entity ↔ DTO conversion. Impelement mapper manually.
- Called from the **Service layer** only. Never called from Controller or Repository.

---

## R4 — Common Layer Rules

`common/` is for reusable infrastructure shared across all domains.

| Allowed in `common/` | Forbidden in `common/` |
|---|---|
| Spring `@Configuration` classes | Business logic of any domain |
| Security filters, JWT utility | Domain-specific entities or DTOs |
| `GlobalExceptionHandler` (`@RestControllerAdvice`) | Domain-specific service logic |
| Standardized `ApiResponse<T>` wrapper | Domain-specific repositories |
| App-wide constants and enums | Feature-specific exception types (put in domain) |
| Pure utility classes (date, string, etc.) | — |

---

## R5 — Security Architecture

- Spring Security is the ONLY security mechanism. Do not implement custom authentication outside of it.
- Security filter chain executes before any Controller.
- JWT-based stateless authentication: validate token in a `OncePerRequestFilter` and set `SecurityContextHolder`.
- Use RBAC: method-level authorization via `@PreAuthorize("hasRole('ROLE_NAME')")` on Service or Controller methods.
- All endpoints are authenticated by default. Public endpoints must be explicitly permitted in `SecurityFilterChain`.

---

## R6 — API Design Standards

- All endpoints use prefix: `/api/v1/{resource}`.
- Resource names are **plural nouns** in `kebab-case`: `/api/v1/order-items`.
- Use correct HTTP methods: `GET` (read), `POST` (create), `PUT` (full update), `PATCH` (partial update), `DELETE` (remove).
- All responses use a standardized wrapper: `ApiResponse<T>` with fields `success`, `message`, `data`, `timestamp`.
- All error responses follow RFC 7807 `ProblemDetail` (Spring Boot 3 native) or a consistent custom error body.
- Use `@Operation` and `@Tag` (springdoc-openapi) on every Controller method.

---

## R7 — Exception Handling

- One `@RestControllerAdvice` class in `common/exception/` handles ALL exceptions globally.
- NEVER use `try-catch` in service methods for flow control. Use it only for wrapping checked exceptions into unchecked domain exceptions.
- Every domain MUST define its own typed exceptions (e.g., `OrderNotFoundException`, `InsufficientStockException`) that extend `RuntimeException`.
- The global handler maps exception types to HTTP status codes and returns a standardized error response.

---

## R8 — Database & Persistence Standards

- Database: PostgreSQL (primary). MySQL acceptable if project requires it.
- ORM: JPA / Hibernate via Spring Data JPA.
- Schema management: (now skip flyway, use only hibernate ddl create or update)
- All tables MUST include: `id` (PK), `created_at`, `updated_at`.
- Use `snake_case` for all table and column names. Map via `@Column(name = "column_name")` if entity field uses `camelCase`.
- Apply indexes on: all foreign key columns, all columns used in `WHERE` or `ORDER BY` in frequent queries.

---

## R9 — Transaction Rules

| Rule | Detail |
|---|---|
| `@Transactional` belongs in | Service layer only |
| Read-only methods | `@Transactional(readOnly = true)` — always |
| Do NOT use `@Transactional` on | Controller methods or Repository methods |
| Cross-service transactions | Avoid. Use domain events or explicit compensation logic instead. |
| Transaction scope | Keep as narrow as possible. Do not wrap entire service methods if only part needs a transaction. |

---

## R10 — Configuration Management

- Use `application.yml` (YAML only, not `.properties`).
- Environment-specific overrides: `application-dev.yml`, `application-prod.yml`, `application-test.yml`.
- Activate profile via `APP_PROFILE` environment variable: `spring.profiles.active=${APP_PROFILE:dev}`.
- All configurable values are bound to a `@ConfigurationProperties` class with `@Validated`.
- No `@Value` for complex or grouped configuration. No hardcoded URLs, credentials, or parameters.

---

## R11 — Event-Driven Decoupling (Optional, Use When Needed)

- Use Spring Application Events (`ApplicationEventPublisher`) to decouple side effects from the primary transaction.
- Examples: `UserRegisteredEvent` → triggers welcome email, `OrderPlacedEvent` → triggers inventory decrement.
- Event listeners run in the same transaction by default. Use `@Async` + `@TransactionalEventListener(phase = AFTER_COMMIT)` for post-commit side effects.
- Do NOT use events as a substitute for proper service orchestration within the same transaction.

---

## R12 — Naming Conventions

| Element | Convention | Example |
|---|---|---|
| Package | lowercase, singular, domain-named | `domain.order`, `domain.payment` |
| Class | `PascalCase` | `OrderController`, `OrderServiceImpl` |
| Interface | `PascalCase`, no `I` prefix | `OrderService` (not `IOrderService`) |
| Method | `camelCase`, verb-first | `createOrder()`, `findByCustomerId()` |
| Constant | `UPPER_SNAKE_CASE` | `MAX_RETRY_ATTEMPTS` |
| DB table | `snake_case`, plural | `order_items` |
| DB column | `snake_case` | `customer_id`, `created_at` |
| REST path | `kebab-case`, plural | `/api/v1/order-items` |
| DTO | `{Action}{Domain}Request/Response` | `CreateOrderRequest`, `OrderResponse` |
| Mapper | `{Domain}Mapper` | `OrderMapper` |
| Exception | `{Reason}Exception` | `OrderNotFoundException` |

---

## R13 — Prohibited Patterns

| Pattern | Reason |
|---|---|
| Horizontal layer packages (`controllers/`, `services/`) | Breaks domain isolation; increases coupling |
| Business logic in Controller | Controller is a routing + validation delegate only |
| Business logic in Repository | Repository is a data access contract only |
| `@Entity` returned from Controller or Service API | Exposes persistence model; violates encapsulation |
| `EAGER` fetch on `@OneToMany` / `@ManyToMany` | Causes unbounded SQL JOINs; N+1 risk |
| `ddl-auto=update` in production | Unpredictable schema changes; data loss risk |
| One service calling another domain's repository | Violates domain ownership |
| Hardcoded URLs, credentials, or configuration values | Not environment-portable; security risk |
| `try-catch` for flow control in service methods | Use domain exceptions + global handler instead |
| Unbounded `findAll()` without pageable | Loads entire table into memory |