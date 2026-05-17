# Rider Module Implementation Plan

Implement the Rider domain to manage rider registration, rider profiles, and delivery/order management, adhering to the `ai-rules.md` architectural guidelines.

## User Review Required

> [!IMPORTANT]
> The `Order` domain currently does not exist in the project structure (`com.delicious.domain.order`). To implement APIs that interact with orders (e.g., `GET /api/v1/riders/orders/{riderId}` and `PUT /api/v1/riders/status/{orderId}`), we need to create a basic `Order` entity. I have included a minimal `Order` domain in this plan. Please review if this approach is acceptable.

## Open Questions

> [!WARNING]
> 1. For `PUT /api/v1/riders/status/{orderId}`, how is a rider assigned to an order? Should there be an API for the rider to "Accept" an order, thereby setting their `riderId` on the `Order`? (I will assume `PUT /api/v1/riders/status/{orderId}` handles both accepting delivery and updating status based on the payload).
> 2. Should the base path be `/api/v1/riders` to follow Rule R6 (`/api/v1/{resource}`)? (I will use `/api/v1/riders` by default).

## Proposed Changes

### `com.delicious.domain.rider` [NEW DOMAIN]

#### [NEW] `entity/RiderProfile.java`
- Fields: `id`, `user` (OneToOne with User), `vehicleType`, `vehicleRegistrationNumber`, `currentLocation` (String or coordinates).

#### [NEW] `dto/RiderRegistrationRequest.java`
- Fields: `name`, `email`, `phone`, `password`, `vehicleType`, `vehicleRegistrationNumber`.

#### [NEW] `dto/RiderResponse.java`
- Fields: `userId`, `name`, `email`, `phone`, `vehicleType`, `vehicleRegistrationNumber`, `status` (UserStatus).

#### [NEW] `dto/DeliveryStatusUpdateRequest.java`
- Fields: `status` (String/Enum - e.g., ACCEPTED, PICKED_UP, DELIVERED).
- Notes: Used to accept an order or update its delivery status.

#### [NEW] `service/RiderService.java` & `RiderServiceImpl.java`
- `registerRider(RiderRegistrationRequest request)`: Creates a User with `ROLE_RIDER` and a `RiderProfile`.
- `getAllRiders(Pageable pageable)`: Returns a paginated list of riders.
- `getRiderById(Long userId)`: Returns rider profile details.
- `getRiderOrders(Long riderId, Pageable pageable)`: Fetches orders assigned to this rider (depends on `Order` entity).
- `updateDeliveryStatus(Long orderId, DeliveryStatusUpdateRequest request)`: Updates the order's status. If status is `ACCEPTED`, it also assigns the current rider to the order.

#### [NEW] `controller/RiderController.java`
- `POST /api/v1/riders` -> `registerRider`
- `GET /api/v1/riders` -> `getAllRiders`
- `GET /api/v1/riders/{id}` -> `getRiderById`
- `GET /api/v1/riders/orders/{riderId}` -> `getRiderOrders`
- `PUT /api/v1/riders/status/{orderId}` -> `updateDeliveryStatus`

---

### `com.delicious.domain.order` [NEW DOMAIN]

*To support the rider delivery features, a minimal Order domain is required.*

#### [NEW] `entity/Order.java`
- Fields: `id`, `customerId`, `sellerId`, `riderId` (nullable), `status` (Enum: PENDING, ACCEPTED_BY_RIDER, PICKED_UP, DELIVERED, CANCELLED), `deliveryAddress`.

#### [NEW] `enums/OrderStatus.java`
- Values: `PENDING`, `ACCEPTED_BY_RIDER`, `PICKED_UP`, `DELIVERED`, `CANCELLED`.

#### [NEW] `repository/OrderRepository.java`
- `Page<Order> findByRiderId(Long riderId, Pageable pageable)`

#### [NEW] `dto/OrderResponse.java`
- Basic representation of an order for the rider's view.

---

### `com.delicious.domain.user` [MODIFY]

#### [MODIFY] `service/UserService.java` & `UserServiceImpl.java`
- Ensure `createUser` is reusable or can be called by `RiderService` (if not already public and generic).

## Verification Plan

1. **Rider Registration**:
   - Call `POST /api/v1/riders` to create a new rider. Verify `User` and `RiderProfile` are created in DB.
2. **Fetch Riders**:
   - Call `GET /api/v1/riders` and `GET /api/v1/riders/{id}`.
3. **Delivery Management**:
   - Manually insert a dummy `Order` in the DB.
   - Call `PUT /api/v1/riders/status/{orderId}` with status `ACCEPTED_BY_RIDER`. Verify order is updated.
   - Call `GET /api/v1/riders/orders/{riderId}` to see the assigned order.
