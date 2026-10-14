package com.delicious.domain.admin.service;

import com.delicious.domain.admin.dto.AdminOrderStatsProjection;
import com.delicious.domain.admin.dto.AdminStatsResponse;
import com.delicious.domain.admin.dto.AdminUserCountProjection;
import com.delicious.domain.order.repository.OrderRepository;
import com.delicious.domain.product.repository.ProductRepository;
import com.delicious.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    /**
     * Fetches all dashboard statistics using only 3 database queries instead of the
     * previous 8 separate COUNT / SUM calls:
     *
     *   Query 1 — userRepository.countAllRoles()
     *             → Returns CUSTOMER / SELLER / RIDER counts in ONE conditional-aggregation query.
     *
     *   Query 2 — orderRepository.getOrderStats()
     *             → Returns totalOrders, pendingOrders, deliveredOrders, totalRevenue in ONE query.
     *
     *   Query 3 — productRepository.count()
     *             → Simple COUNT(*) on the products table.
     */
    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getDashboardStats() {
        // --- Query 1: all user role counts in a single conditional-aggregation pass ---
        AdminUserCountProjection userCounts = userRepository.countAllRoles();

        // --- Query 2: all order stats (total, pending, delivered, revenue) in one pass ---
        AdminOrderStatsProjection orderStats = orderRepository.getOrderStats();

        // --- Query 3: total product count ---
        long totalProducts = productRepository.count();

        return AdminStatsResponse.builder()
                .totalCustomers(userCounts.getCustomers())
                .totalSellers(userCounts.getSellers())
                .totalRiders(userCounts.getRiders())
                .totalProducts(totalProducts)
                .totalOrders(orderStats.getTotalOrders())
                .pendingOrders(orderStats.getPendingOrders())
                .deliveredOrders(orderStats.getDeliveredOrders())
                .totalRevenue(orderStats.getTotalRevenue())
                .build();
    }
}

