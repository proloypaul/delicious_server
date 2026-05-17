package com.delicious.domain.rider.service;

import com.delicious.domain.order.dto.OrderResponse;
import com.delicious.domain.rider.dto.DeliveryStatusUpdateRequest;
import com.delicious.domain.rider.dto.RiderRegistrationRequest;
import com.delicious.domain.rider.dto.RiderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RiderService {
    RiderResponse registerRider(RiderRegistrationRequest request);
    Page<RiderResponse> getAllRiders(Pageable pageable);
    RiderResponse getRiderById(Long userId);
    Page<OrderResponse> getRiderOrders(Long riderId, Pageable pageable);
    OrderResponse updateDeliveryStatus(Long orderId, DeliveryStatusUpdateRequest request, Long riderId);
}
