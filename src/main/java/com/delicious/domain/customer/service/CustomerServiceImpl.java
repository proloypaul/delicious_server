package com.delicious.domain.customer.service;

import com.delicious.domain.customer.dto.CustomerProfileResponse;
import com.delicious.domain.customer.dto.UpdateCustomerProfileRequest;
import com.delicious.domain.customer.entity.CustomerProfile;
import com.delicious.domain.customer.mapper.CustomerMapper;
import com.delicious.domain.customer.repository.CustomerProfileRepository;
import com.delicious.domain.user.entity.User;
import com.delicious.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final UserService userService;
    private final CustomerProfileRepository customerProfileRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(Long userId) {
        User user = userService.getUserById(userId);
        CustomerProfile profile = customerProfileRepository.findByUserId(userId).orElse(null);
        return customerMapper.toProfileResponse(user, profile);
    }

    @Override
    @Transactional
    public CustomerProfileResponse updateProfile(Long userId, UpdateCustomerProfileRequest request) {
        User updatedUser = userService.updateProfile(
                userId,
                request.getName(),
                request.getPhone()
        );

        CustomerProfile profile = customerProfileRepository.findByUserId(userId)
                .orElseGet(() -> CustomerProfile.builder().user(updatedUser).build());

        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }

        profile = customerProfileRepository.save(profile);

        return customerMapper.toProfileResponse(updatedUser, profile);
    }
}
