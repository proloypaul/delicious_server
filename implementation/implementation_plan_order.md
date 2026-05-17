# Order Module Implementation Plan

Implement the Order domain to handle order creation, tracking, and management, adhering to the `ai-rules.md` architectural guidelines. This plan must be executed *before* the Rider module.

## User Review Required

> [!IMPORTANT]
> - **Product List Representation**: To store the `products (list)`, I propose creating an `OrderItem` entity that links an `Order` to a `productId` with `quantity` and `price`. This avoids storing raw JSON and adheres to relational database best practices.
> - **Cross-Domain Data Fetching**: To include Customer and Rider data in every Order response, the `OrderService` will fetch these details using `CustomerService` and `RiderService` (or `UserService`), injecting them into the `OrderResponse` DTO. This ensures domain isolation instead of direct table joins across bounded contexts.

## Proposed Changes

### `com.delicious.domain.order` [NEW DOMAIN]

#### [NEW] `entity/Order.java`
- Fields: `id`, `phone`, `address`, `customerId` (Long), `riderId` (Long, nullable), `subTotal` (BigDecimal), `discount` (BigDecimal), `totalAmount` (BigDecimal), `orderStatus` (Enum).
- Relationships: `@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)` with `OrderItem`.

#### [NEW] `entity/OrderItem.java`
- Fields: `id`, `order` (@ManyToOne), `productId` (Long), `quantity` (Integer), `price` (BigDecimal).

#### [NEW] `enums/OrderStatus.java`
- Values: `PENDING`, `PROCESSING`, PENDING, ACCEPTED_BY_RIDER, PICKED_UP, DELIVERED.

#### [NEW] `dto/OrderRequest.java`
- Fields: `phone`, `address`, `discount` (optional), `items` (List of `OrderItemRequest` containing `productId` and `quantity`).

#### [NEW] `dto/OrderResponse.java`
- Fields: `id`, `phone`, `address`, `subTotal`, `discount`, `totalAmount`, `orderStatus`, `items` (List of `OrderItemResponse`).
- **Enriched Fields**: `customer` (CustomerSummaryDto) and `rider` (RiderSummaryDto) to satisfy the requirement of including relationship data in every response.

#### [NEW] `dto/OrderStatusUpdateRequest.java`
- Fields: `orderId` (Long), `status` (OrderStatus).

#### [NEW] `service/OrderService.java` & `OrderServiceImpl.java`
- `createOrder(OrderRequest request, Long customerId)`: Iterates over products to fetch current prices, calculates `subTotal`, subtracts `discount`, and sets `totalAmount`. Saves `Order` and `OrderItem`s.
- `getOrderById(Long id)`: Fetches order and maps to `OrderResponse` (enriching with customer/rider data).
- `getOrdersByCustomer(Long customerId, Pageable pageable)`: Returns paginated orders for a customer.
- `getAllOrders(Pageable pageable)`: Returns all orders. In the mapper/service, it will fetch customer and rider details for each order to include in the response.
- `updateOrderStatus(Long orderId, OrderStatus status)`: Updates order status.

#### [NEW] `controller/OrderController.java`
- `POST /api/v1/orders` -> `createOrder`
- `GET /api/v1/orders/{id}` -> `getOrderById`
- `GET /api/v1/orders/customer` -> `getOrdersByCustomer`
- `GET /api/v1/orders` -> `getAllOrders`
- `PUT /api/v1/orders/status` -> `updateOrderStatus`

---

### Integration Points

#### `com.delicious.domain.customer` [MODIFY]
- Expose a method in `CustomerService` (e.g., `getCustomerSummary(Long customerId)`) to easily fetch customer data for the `OrderResponse`.

#### `com.delicious.domain.product` [MODIFY]
- Expose a method in `ProductService` to fetch prices for given `productId`s to accurately calculate the order subTotal.

## Verification Plan

1. **Order Creation**:
   - `POST /api/v1/orders` with product IDs.
   - Verify `totalAmount` is calculated correctly inside the service based on `subTotal - discount`.
2. **Data Enrichment**:
   - `GET /api/v1/orders` and `GET /api/v1/orders/{id}`.
   - Verify the JSON response contains nested `customer` and `rider` objects.
3. **Status Update**:
   - `PUT /api/v1/orders/status` with `PROCESSING` or `COMPLETED`.
   - Verify DB reflects the new status.
