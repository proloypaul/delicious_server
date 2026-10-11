-- =============================================================================
-- V2 Migration: Add Missing Indexes and Database Constraints
-- =============================================================================
-- This migration adds:
--   1. Missing indexes on high-frequency query columns (fixes N+1 & slow scans)
--   2. Missing Foreign Key constraints (enforces referential integrity)
--   3. Missing Unique constraints (prevents duplicate data)
--   4. CHECK constraints (validates data format at DB level)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- SECTION 1: INDEXES
-- -----------------------------------------------------------------------------

-- users.phone: queried in existsByPhone() during Rider/Seller registration
CREATE INDEX IF NOT EXISTS idx_users_phone  ON users(phone);

-- users.role: queried in countByRole() and getUsersByRole() for admin stats
CREATE INDEX IF NOT EXISTS idx_users_role   ON users(role);

-- users.status: queried for filtering active/inactive users
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- orders.customer_id: queried in findByCustomerId() — called on every
--   customer dashboard page load. Without this index, full table scan occurs.
CREATE INDEX IF NOT EXISTS idx_orders_customer_id  ON orders(customer_id);

-- orders.rider_id: queried in findByRiderId() for rider dashboard orders
CREATE INDEX IF NOT EXISTS idx_orders_rider_id     ON orders(rider_id);

-- orders.order_status: queried in countByOrderStatus() for admin statistics
CREATE INDEX IF NOT EXISTS idx_orders_order_status ON orders(order_status);

-- order_items.order_id: queried when JPA loads the @OneToMany items collection.
--   Without this, every Order→items access causes a full order_items table scan.
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);

-- reviews.product_id: queried in findByProductId() — called on every product page
CREATE INDEX IF NOT EXISTS idx_reviews_product_id  ON reviews(product_id);

-- reviews.customer_id: queried when checking for existing reviews per customer
CREATE INDEX IF NOT EXISTS idx_reviews_customer_id ON reviews(customer_id);


-- -----------------------------------------------------------------------------
-- SECTION 2: FOREIGN KEY CONSTRAINTS (Referential Integrity)
-- -----------------------------------------------------------------------------

-- orders.customer_id → users(id)
--   Ensures every order belongs to an existing user. ON DELETE RESTRICT
--   prevents deleting a user who has placed orders.
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE RESTRICT;

-- orders.rider_id → users(id)
--   Nullable FK — a rider is only assigned after order is accepted.
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_rider
        FOREIGN KEY (rider_id) REFERENCES users(id) ON DELETE SET NULL;

-- order_items.product_id → products(id)
--   Snapshot price is stored on the item, but we still keep the FK reference.
--   ON DELETE RESTRICT prevents deleting a product that is part of an order.
ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT;

-- reviews.product_id → products(id)
ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_product
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

-- reviews.customer_id → users(id)
ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_customer
        FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE;

-- products.seller_id → users(id)
--   Ensures every product belongs to an existing user (with SELLER role).
ALTER TABLE products
    ADD CONSTRAINT fk_products_seller
        FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE RESTRICT;


-- -----------------------------------------------------------------------------
-- SECTION 3: UNIQUE CONSTRAINTS
-- -----------------------------------------------------------------------------

-- Prevent a customer from leaving more than one review per product
ALTER TABLE reviews
    ADD CONSTRAINT uq_reviews_customer_product
        UNIQUE (customer_id, product_id);


-- -----------------------------------------------------------------------------
-- SECTION 4: CHECK CONSTRAINTS (Data Validation at DB Level)
-- -----------------------------------------------------------------------------

-- Enforce minimum/maximum phone number length
ALTER TABLE users
    ADD CONSTRAINT chk_users_phone_length
        CHECK (char_length(phone) >= 10 AND char_length(phone) <= 15);

-- Enforce product price is positive
ALTER TABLE products
    ADD CONSTRAINT chk_products_price_positive
        CHECK (price > 0);

-- Enforce review rating is between 1 and 5
ALTER TABLE reviews
    ADD CONSTRAINT chk_reviews_rating_range
        CHECK (rating >= 1 AND rating <= 5);

-- Enforce order amounts are non-negative
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_subtotal_non_negative   CHECK (sub_total >= 0);
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_discount_non_negative   CHECK (discount >= 0);
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_total_non_negative      CHECK (total_amount >= 0);
