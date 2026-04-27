# Customer Module Implementation Plan

This plan details the implementation of the Customer features (Profile viewing and updating), adhering strictly to the `ai-rules.md` architectural guidelines.

## Goal Description
Implement the Customer domain features to allow a customer to view and update their profile (name, phone, address). This involves adding an `address` field to the `User` entity, creating `UserService` for cross-domain communication, and building the `CustomerController` and `CustomerService`.

## User Review Required
> [!IMPORTANT]
> A copy of this plan will also be saved to the `implementation/` directory as `implementation_plan02.md`.

## Proposed Changes

---

### `com.delicious.domain.user`
*Since `name` and `phone` already exist on the `User` entity, we will add `address` here and create a `UserService` to allow the Customer domain to interact with User data without violating dependency rules.*

#### [MODIFY] `entity/User.java`
- Add `private String address;`

#### [NEW] `service/UserService.java` & `service/UserServiceImpl.java`
- `User getUserById(Long id)`
- `User updateProfile(Long id, String name, String phone, String address)`
- *Note: `CustomerService` will call `UserService` to perform updates, respecting cross-domain rules.*

---

### `com.delicious.domain.customer`
*This is the new feature domain specifically handling customer-facing logic.*

#### [NEW] `dto/CustomerProfileResponse.java`
- Fields: `name`, `email`, `phone`, `address`, `status`.

#### [NEW] `dto/UpdateCustomerProfileRequest.java`
- Fields: `name` (@NotBlank), `phone` (@NotBlank), `address`.

#### [NEW] `mapper/CustomerMapper.java`
- Maps `User` to `CustomerProfileResponse`.

#### [NEW] `service/CustomerService.java` & `service/CustomerServiceImpl.java`
- `CustomerProfileResponse getProfile(Long userId)`: Calls `userService.getUserById()`.
- `CustomerProfileResponse updateProfile(Long userId, UpdateCustomerProfileRequest request)`: Calls `userService.updateProfile()`.

#### [NEW] `controller/CustomerController.java`
- Base path: `/api/v1/customers`
- `GET /profile`: Returns `ApiResponse<CustomerProfileResponse>`.
- `PUT /profile`: Accepts `UpdateCustomerProfileRequest`, returns `ApiResponse<CustomerProfileResponse>`.

## Open Questions
1. **User Authentication Context:** We haven't implemented JWT parsing yet. For the `GET /profile` and `PUT /profile` endpoints, how should we identify the user? 
   - *Option A:* Pass `?userId=X` as a query parameter temporarily until JWT is implemented.
   - *Option B:* Implement the JWT filter now to extract the user ID from the `SecurityContext`.
   - *I will proceed with Option A (query parameter `userId`) for testing unless you specify otherwise.*
2. **Domain Separation:** Is adding `address` to the `User` entity acceptable, or would you prefer a separate `CustomerProfile` entity with a `@OneToOne` mapping to `User`?

## Verification Plan
1. **Compilation:** Verify the project compiles.
2. **Testing:**
   - Call `GET /api/v1/customers/profile?userId=1` and ensure it returns the user's data.
   - Call `PUT /api/v1/customers/profile?userId=1` to update `name`, `phone`, and `address`, then verify the changes are persisted in the database.
