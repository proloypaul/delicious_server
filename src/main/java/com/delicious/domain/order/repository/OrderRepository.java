package com.delicious.domain.order.repository;

import com.delicious.domain.admin.dto.AdminOrderStatsProjection;
import com.delicious.domain.order.entity.Order;
import com.delicious.domain.order.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Paginated customer orders — uses Entity Graph to JOIN FETCH items
     * in one SQL query, preventing N+1 on the items collection.
     */
    @EntityGraph(value = "Order.withItems", type = EntityGraph.EntityGraphType.LOAD)
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * Paginated rider orders — uses Entity Graph to JOIN FETCH items.
     */
    @EntityGraph(value = "Order.withItems", type = EntityGraph.EntityGraphType.LOAD)
    Page<Order> findByRiderId(Long riderId, Pageable pageable);

    /**
     * All orders paginated — uses Entity Graph to prevent N+1 on items.
     */
    @EntityGraph(value = "Order.withItems", type = EntityGraph.EntityGraphType.LOAD)
    Page<Order> findAll(Pageable pageable);

    /**
     * Pessimistic write lock — used by riders accepting an order.
     * Acquires a row-level DB lock so only ONE rider can claim the order.
     * Any concurrent transaction attempting the same order will wait/fail.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLock(@Param("id") Long id);

    /** Admin stat: count orders by status. */
    long countByOrderStatus(OrderStatus orderStatus);

    /**
     * Admin stat: total revenue from delivered orders.
     * Uses enum parameter instead of hardcoded string to prevent typo bugs.
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderStatus = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") OrderStatus status);

    /**
     * Returns total orders, pending orders, delivered orders and total revenue in a
     * SINGLE database round-trip using conditional aggregation.
     * Replaces four separate count/sum calls in AdminServiceImpl.
     */
    @Query("""
            SELECT
                COUNT(o)                                                                      AS totalOrders,
                SUM(CASE WHEN o.orderStatus = com.delicious.domain.order.enums.OrderStatus.PENDING   THEN 1 ELSE 0 END) AS pendingOrders,
                SUM(CASE WHEN o.orderStatus = com.delicious.domain.order.enums.OrderStatus.DELIVERED THEN 1 ELSE 0 END) AS deliveredOrders,
                COALESCE(SUM(CASE WHEN o.orderStatus = com.delicious.domain.order.enums.OrderStatus.DELIVERED THEN o.totalAmount ELSE 0 END), 0) AS totalRevenue
            FROM Order o
            """)
    AdminOrderStatsProjection getOrderStats();
}
