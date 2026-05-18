package com.delicious.domain.order.repository;

import com.delicious.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    Page<Order> findByRiderId(Long riderId, Pageable pageable);
    long countByOrderStatus(com.delicious.domain.order.enums.OrderStatus orderStatus);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderStatus = 'DELIVERED'")
    java.math.BigDecimal sumTotalAmountByDeliveredStatus();
}
