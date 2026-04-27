# Product and Category Module Implementation Plan

This plan details the creation of the `Product` and `Category` module within the `product` domain. It focuses on setting up the necessary entities, repositories, services, and APIs while strictly adhering to your `ai-rules.md`.

## Goal Description
Implement `Category` and `Product` entities with a `@ManyToOne` relationship. Expose a full set of CRUD operations for `Product` along with filtering by `categoryId`. Implement data fetching strategies that eliminate the N+1 query problem.

## User Review Required
> [!IMPORTANT]
> Please review this plan. Upon your approval, a copy will be saved as `implementation/implementation_plan03.md` and execution will begin.

## Proposed Changes

### `com.delicious.domain.product`
*This new feature domain will encapsulate both Product and Category, as they are tightly coupled.*

#### [NEW] `enums/ProductStatus.java`
- `PENDING`, `APPROVED`

#### [NEW] `entity/Category.java`
- Fields: `name`, `image`.
- Inherits `BaseEntity`.

#### [NEW] `entity/Product.java`
- Fields: `foodName`, `description`, `price` (BigDecimal), `makingTime` (Integer), `status` (Enum), `sellerId` (Long).
- `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id") private Category category;`
- Inherits `BaseEntity`.

#### [NEW] `repository/CategoryRepository.java`
- Standard JPA repository.

#### [NEW] `repository/ProductRepository.java`
- Standard JPA repository.
- **N+1 Solution:** Use Spring Data JPA's `@EntityGraph(attributePaths = {"category"})` on query methods like `findAll(Pageable)` and `findByCategoryId(Long categoryId, Pageable)` to automatically `JOIN FETCH` the category in a single query.

#### [NEW] `dto/CategoryResponse.java` & `dto/ProductResponse.java`
- Response objects for the entities. `ProductResponse` will include the nested `CategoryResponse`.

#### [NEW] `dto/ProductRequest.java`
- DTO for creation and updates. Contains `foodName`, `description`, `price`, `makingTime`, `status`, `sellerId`, `categoryId`.

#### [NEW] `mapper/ProductMapper.java` & `mapper/CategoryMapper.java`
- Mappers to convert entities to DTOs and vice versa.

#### [NEW] `service/ProductService.java` & `service/ProductServiceImpl.java`
- Implements CRUD logic.
- Validates the existence of the `Category` before saving a `Product`.

#### [NEW] `controller/ProductController.java`
- `POST /api/v1/products`
- `GET /api/v1/products` (Accepts `?categoryId=` for filtering, and pagination parameters)
- `GET /api/v1/products/{id}`
- `PUT /api/v1/products/{id}`
- `DELETE /api/v1/products/{id}`

## Open Questions
1. **Category Management:** Do we also need APIs to create/update/delete Categories (`POST /api/v1/categories`), or will you seed them manually for now? (I will only implement the Product APIs requested unless told otherwise).
2. **Pricing Type:** I'll use `java.math.BigDecimal` for `price`. Is that acceptable?

## Verification Plan
1. Compile the project.
2. Ensure database schema automatically creates `categories` and `products` tables.
3. Test `GET /api/v1/products` using Swagger UI to verify that the generated SQL logs contain a `LEFT OUTER JOIN` (avoiding N+1 queries).
