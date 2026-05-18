package com.delicious.domain.admin.service;

import com.delicious.domain.admin.dto.AdminStatsResponse;
import com.delicious.domain.user.enums.UserRole;
import com.delicious.domain.user.repository.UserRepository;
import com.delicious.domain.product.repository.ProductRepository;
import com.delicious.domain.order.repository.OrderRepository;
import com.delicious.domain.order.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getDashboardStats() {
        long totalCustomers = userRepository.countByRole(UserRole.CUSTOMER);
        long totalSellers = userRepository.countByRole(UserRole.SELLER);
        long totalRiders = userRepository.countByRole(UserRole.RIDER);
        
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        
        long pendingOrders = orderRepository.countByOrderStatus(OrderStatus.PENDING);
        long deliveredOrders = orderRepository.countByOrderStatus(OrderStatus.DELIVERED);
        
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByDeliveredStatus();

        return AdminStatsResponse.builder()
                .totalCustomers(totalCustomers)
                .totalSellers(totalSellers)
                .totalRiders(totalRiders)
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .deliveredOrders(deliveredOrders)
                .totalRevenue(totalRevenue)
                .build();
    }
}
