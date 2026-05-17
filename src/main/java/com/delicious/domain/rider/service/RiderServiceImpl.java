package com.delicious.domain.rider.service;

import com.delicious.domain.order.dto.OrderResponse;
import com.delicious.domain.order.entity.Order;
import com.delicious.domain.order.enums.OrderStatus;
import com.delicious.domain.order.exception.OrderNotFoundException;
import com.delicious.domain.order.repository.OrderRepository;
import com.delicious.domain.order.service.OrderService;
import com.delicious.domain.rider.dto.DeliveryStatusUpdateRequest;
import com.delicious.domain.rider.dto.RiderRegistrationRequest;
import com.delicious.domain.rider.dto.RiderResponse;
import com.delicious.domain.rider.entity.RiderProfile;
import com.delicious.domain.rider.mapper.RiderMapper;
import com.delicious.domain.rider.repository.RiderProfileRepository;
import com.delicious.domain.user.entity.User;
import com.delicious.domain.user.enums.UserRole;
import com.delicious.domain.user.enums.UserStatus;
import com.delicious.domain.user.exception.UserAlreadyExistsException;
import com.delicious.domain.user.repository.UserRepository;
import com.delicious.domain.rider.exception.RiderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RiderServiceImpl implements RiderService {

    private final UserRepository userRepository;
    private final RiderProfileRepository riderProfileRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;
    private final RiderMapper riderMapper;

    @Override
    @Transactional
    public RiderResponse registerRider(RiderRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("Phone number is already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.RIDER)
                .status(UserStatus.INACTIVE) // Requires admin approval possibly
                .build();

        user = userRepository.save(user);

        RiderProfile profile = RiderProfile.builder()
                .user(user)
                .vehicleType(request.getVehicleType())
                .vehicleRegistrationNumber(request.getVehicleRegistrationNumber())
                .build();

        profile = riderProfileRepository.save(profile);

        return riderMapper.toRiderResponse(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RiderResponse> getAllRiders(Pageable pageable) {
        return riderProfileRepository.findAll(pageable)
                .map(profile -> riderMapper.toRiderResponse(profile.getUser(), profile));
    }

    @Override
    @Transactional(readOnly = true)
    public RiderResponse getRiderById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RiderNotFoundException("User not found with id: " + userId));

        RiderProfile profile = riderProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RiderNotFoundException("Rider profile not found for user: " + userId));

        return riderMapper.toRiderResponse(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getRiderOrders(Long riderId, Pageable pageable) {
        return orderService.getOrdersByRider(riderId, pageable);
    }

    @Override
    @Transactional
    public OrderResponse updateDeliveryStatus(Long orderId, DeliveryStatusUpdateRequest request, Long riderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (request.getStatus() == OrderStatus.ACCEPTED_BY_RIDER) {
            order.setRiderId(riderId);
        } else {
            // Verify if the order belongs to this rider
            if (order.getRiderId() == null || !order.getRiderId().equals(riderId)) {
                throw new IllegalStateException("You are not assigned to this order");
            }
        }

        order.setOrderStatus(request.getStatus());
        orderRepository.save(order);

        return orderService.getOrderById(orderId);
    }
}
