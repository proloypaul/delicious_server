# Seller Module Implementation Plan

Implement the Seller domain to manage store information, seller registration, and provide seller-specific product management, adhering to the `ai-rules.md` architectural guidelines.

## Goal Description
- Implement **Seller Registration**: `POST /api/v1/sellers/register` will create a new user with `ROLE_SELLER` and a default status of `INACTIVE`.
- Create a `SellerProfile` entity to store store-specific information (store name, etc.).
- Implement APIs for sellers to view/update their profile and view their own products.
- Enhance the product listing to include seller details (name, store name, ID).
- Ensure strict domain isolation and security.

## Proposed Changes

### `com.delicious.domain.seller` [NEW DOMAIN]
*This domain will handle all store/seller specific logic.*

#### [NEW] `entity/SellerProfile.java`
- Fields: `id`, `user` (OneToOne with User), `storeName`, `description`, `address`.

#### [NEW] `dto/SellerRegistrationRequest.java`
- Fields: `name`, `email`, `phone`, `password`, `storeName`.

#### [NEW] `dto/SellerProfileResponse.java`
- Fields: `userId`, `name`, `email`, `storeName`, `description`, `address`, `status`.

#### [NEW] `dto/UpdateSellerProfileRequest.java`
- Fields: `storeName` (@NotBlank), `description`, `address`.

#### [NEW] `service/SellerService.java` & `SellerServiceImpl.java`
- `register(SellerRegistrationRequest request)`: Creates `User` (ROLE_SELLER, INACTIVE) and initial `SellerProfile`.
- `getProfile(Long userId)`: Fetches User and SellerProfile.
- `updateProfile(Long userId, UpdateSellerProfileRequest request)`: Updates SellerProfile.
- `getSellerProducts(Long userId, Pageable pageable)`: Fetches products filtered by sellerId.

#### [NEW] `controller/SellerController.java`
- `POST /api/v1/sellers/register`
- `GET /api/v1/sellers/profile?userId=X`
- `PUT /api/v1/sellers/profile?userId=X`
- `GET /api/v1/sellers/products?userId=X`

---

### `com.delicious.domain.product` [MODIFY]
#### [MODIFY] `dto/ProductResponse.java`
- Add `sellerId`, `sellerName`, `storeName`.

#### [MODIFY] `service/ProductService.java` & `ProductServiceImpl.java`
- Update `getAllProducts` to include seller details in the response.

---

### `com.delicious.domain.user` [MODIFY]
#### [MODIFY] `service/UserService.java` & `UserServiceImpl.java`
- Add `User createUser(String name, String email, String phone, String password, UserRole role, UserStatus status)`.

## Verification Plan
1. **Registration**:
   - `POST /api/v1/sellers/register` -> Verify User created with `INACTIVE` status.
2. **Seller Profile**:
   - `GET /api/v1/sellers/profile?userId=X`
3. **Product Listing**:
   - `GET /api/v1/products` -> Verify `sellerName` and `storeName` are present.
