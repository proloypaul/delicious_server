package com.delicious.domain.admin.dto;

/**
 * JPA projection for the single-query order stats.
 * Maps to the scalar expressions in OrderRepository.getOrderStats().
 */
public interface AdminOrderStatsProjection {
    long getTotalOrders();
    long getPendingOrders();
    long getDeliveredOrders();
    java.math.BigDecimal getTotalRevenue();
}
