# Authentication Module Implementation Plan

This plan details the implementation of the Spring Boot authentication module, adhering strictly to the `ai-rules.md` architectural guidelines (Modular Monolith, Domain-Oriented).

## Goal Description
Implement a robust User authentication system including user registration and login functionality. The system will use PostgreSQL (or H2 in-memory currently configured) via Spring Data JPA, hash passwords securely using BCrypt, and follow a strict layered architecture (`Controller -> Service -> Repository`).

## User Review Required
> [!IMPORTANT]
> A copy of this plan will also be saved to the `implementation/` directory in your project workspace as requested.

## Proposed Changes

We will create a new domain package: `com.delicious.domain.user`. All related components will reside within this feature domain to maintain isolation.

---

### `com.delicious.domain.user`

#### [NEW] `enums/UserRole.java`
- Enum defining roles: `CUSTOMER`, `SELLER`, `RIDER`, `ADMIN`.

#### [NEW] `enums/UserStatus.java`
- Enum defining account statuses: `ACTIVE`, `INACTIVE`, `BANNED`.

#### [NEW] `entity/User.java`
- JPA Entity representing the user.
- Extends `BaseEntity` to inherit `id`, `createdAt`, `updatedAt`.
- Fields: `name`, `email` (unique), `phone`, `password`, `role` (EnumType.STRING), `status` (EnumType.STRING).

#### [NEW] `repository/UserRepository.java`
- Extends `JpaRepository<User, Long>`.
- Method: `Optional<User> findByEmail(String email);`
- Method: `boolean existsByEmail(String email);`

#### [NEW] `dto/RegisterRequest.java`
- Record or Class for registration input.
- Validation: `@NotBlank` on name, email, password; `@Email` on email.

#### [NEW] `dto/LoginRequest.java`
- Record or Class for login input.
- Validation: `@NotBlank` on email, password.

#### [NEW] `dto/AuthResponse.java`
- Response DTO returning user details (excluding password).

#### [NEW] `mapper/UserMapper.java`
- Manual mapping logic to convert `User` to `AuthResponse`.

#### [NEW] `exception/UserAlreadyExistsException.java`
#### [NEW] `exception/InvalidCredentialsException.java`
- Domain-specific runtime exceptions.

#### [NEW] `service/AuthService.java`
- Interface defining `AuthResponse registerUser(RegisterRequest request)` and `AuthResponse loginUser(LoginRequest request)`.

#### [NEW] `service/AuthServiceImpl.java`
- Implements `AuthService`.
- Uses `@Transactional`.
- Hashes passwords using `BCryptPasswordEncoder` during registration.
- Verifies passwords during login.

#### [NEW] `controller/AuthController.java`
- Base path: `/api/v1/auth`.
- `POST /register`: Validates `RegisterRequest` and delegates to `AuthService`. Returns wrapped `ApiResponse<AuthResponse>`.
- `POST /login`: Validates `LoginRequest` and delegates to `AuthService`. Returns wrapped `ApiResponse<AuthResponse>`.

---

### `com.delicious.common.security`

#### [NEW] `SecurityConfig.java`
- Temporarily configures Spring Security to permit all requests to `/api/v1/auth/**` and disable CSRF, exposing the `BCryptPasswordEncoder` bean.

## Open Questions
- Do you want me to generate dummy JWT tokens in the `AuthResponse` right now, or just return the user details upon a successful login and handle the full JWT filter chain implementation in a subsequent step?
- Is it acceptable to place these inside `domain/user` as a unified feature, or do you prefer separating `auth` and `user` into two distinct domains immediately?

## Verification Plan
1. **Compilation:** Verify the project compiles successfully.
2. **Execution:** Start the application.
3. **Testing:**
   - Send `POST /api/v1/auth/register` to create a user and verify BCrypt hashing in the database.
   - Send `POST /api/v1/auth/login` with correct credentials to verify successful authentication.
   - Send `POST /api/v1/auth/login` with incorrect credentials to ensure `InvalidCredentialsException` is thrown.
